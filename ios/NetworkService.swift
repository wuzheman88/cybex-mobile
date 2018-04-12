//
//  NetworkService.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import Starscream
import JSONRPCKit
import SwiftyJSON
import EZSwiftExtensions

enum NodeURLString:String {
  case shanghai = "wss://shanghai.51nebula.com"
  case beijing = "wss://beijing.51nebula.com"
  case hongkong = "wss://hongkong.cybex.io"
  case singapore = "wss://singapore-01.cybex.io"
  case tokyo = "wss://tokyo-01.cybex.io"
  case korea = "wss://korea-01.cybex.io"
  
  static var all:[NodeURLString] {
    return [.shanghai, .beijing, .hongkong, .singapore, .tokyo, .korea]
  }
}


class NetWorkService {
  typealias RPCResponse = ([Any?]) -> ()
  typealias RPCSingleResponse = (Any) -> ()

  var retry:(()->())?
  var autoConnectCount = 0
  var isConnecting:Bool = false

  var socket:WebSocket!
  var testsockets:[WebSocket]!

  var batchFactory:BatchFactory!
  var idGenerator:JsonIdGenerator = JsonIdGenerator()
  var callbacks:[Int: RPCSingleResponse] = [:]

  var currentNode:NodeURLString?
  
  private init() {
    currentNode = NodeURLString.shanghai
    var request = URLRequest(url: URL(string:currentNode!.rawValue)!)
    request.timeoutInterval = 25
    
    socket = WebSocket(request: request)
    socket.delegate = self
    socket.connect()
    isConnecting = true
    batchFactory = BatchFactory(version: "2.0", idGenerator:idGenerator)
  }
  
  static let shared = NetWorkService()
  
  func switchFastNode() {
    currentNode = nil
    
    testsockets = []
    for node in NodeURLString.all {
      
      let testsocket = WebSocket(url: URL(string:node.rawValue)!)
      testsockets.append(testsocket)
      
      //websocketDidConnect
      testsocket.onConnect = {
        if self.currentNode == nil {
          self.currentNode = node
          self.changeNode(node: node)
        }
      }
   
      testsocket.connect()
    }
    
    ez.runThisAfterDelay(seconds: 10, after: {
      if !self.socket.isConnected, self.autoConnectCount <= 5 {
        self.autoConnectCount += 1
        self.autoConnect()
      }
    })
  }
  
  private func changeNode(node: NodeURLString) {
    print("switch node is \(node.rawValue)")
    currentNode = node
    var request = URLRequest(url: URL(string:node.rawValue)!)
    request.timeoutInterval = 25

    socket.request = request
    socket.delegate = self
    socket.connect()
    isConnecting = true
  }
  
  private func write(data:Any, callback:RPCSingleResponse?) {
    var data = JSON(data)
    
    if var params = data["params"].array, let id = data["id"].int , let event = params[1].string, event == dataBaseCatogery.subscribe_to_market.rawValue, var subParams = params[2].array {
      subParams[0] = JSON(id)
      params[2] = JSON(subParams)
      data["params"] = JSON(params)
    }
    
    guard let jsonData = try? data.rawData(), let id = data["id"].int else {
      return
    }
    
    print("post Data:", data)
    socket.write(data: jsonData) {[weak self] in
      guard let `self` = self, let callback = callback else { return }
      
      if self.callbacks.count > 1000 {
        self.callbacks.removeAll()
      }
      
      self.callbacks[id] = callback
    }
  }
  
  func checkNetworAndConnect() {
    if !socket.isConnected {
      self.autoConnectCount = 0
      self.autoConnect()
    }
  }
}

extension NetWorkService {
  private func validMainID() -> Bool {
    return JsonRPCService.shared.existAllIDs()
  }
  
  func send<Request: JSONRPCKit.Request>(_ valid:Bool = true, request: [Request], callback:RPCResponse?) {
    if valid, !validMainID() {
      
      let params = request.flatMap({ (request) -> [Any] in
         var data = request.parameters as! [Any]
         data.removeFirst()
        return data
      })
      let digest = params.reduce("", { (last, cur) -> String in
        return last + "\(cur)"
      })
      
      JsonRPCService.shared.addRetryRequest((digest, request, callback))

      if socket.isConnected {
        JsonRPCService.shared.requestIDs(request)
      }
      else {
        JsonRPCService.shared.requestIDs(request)
        self.retry = {[weak self] in
          guard let `self` = self else { return }
          JsonRPCService.shared.requestIDs(request)
          self.retry = nil
        }
        if !self.isConnecting {
          switchFastNode()
        }
      }

      return
    }
    
    var i = 0
    var responses:[Any?] = Array(repeatElement(nil, count: request.count))
    
    for (idx, re) in request.enumerated() {
      let batch = batchFactory.create(re)
      
      let block:(Any) -> () = { (result) in
        i += 1
        guard let response = try? batch.responses(from: result) else {
          return
        }
        
        responses[idx] = response
        
        let filt = responses.filter { $0 != nil }
        
        if let callback = callback, i == request.count, filt.count == request.count {
          var data = JSON(batch.batchElement.body)
          
          if var params = data["params"].array, let id = data["id"].int , let event = params[1].string, event == dataBaseCatogery.subscribe_to_market.rawValue {
            callback([id])
            return
          }
          
          callback(responses)
        }
      }
      
      NetWorkService.shared.write(data: batch.requestObject, callback: block)
    }

  }
  
  func autoConnect() {
    self.retry = {[weak self] in
      guard let `self` = self else { return }
      
      UIApplication.shared.coordinator().fetchAsset(assetID.all)
      UIApplication.shared.coordinator().getLatestData()
      JsonRPCService.shared.requestIDs([LoginRequest(username: "", password: "")])
      self.retry = nil
    }
    switchFastNode()
  }
}


extension NetWorkService: WebSocketDelegate {
  func websocketDidConnect(socket: WebSocketClient) {
    print("websocket is connected")
    JsonRPCService.shared.isFetchingID = false
    self.autoConnectCount = 0
    self.isConnecting = false
    self.retry?()
  }
  
  func websocketDidDisconnect(socket: WebSocketClient, error: Error?) {
    if let e = error as? WSError {
      print("websocket is disconnected: \(e.message)")
    } else if let e = error {
      print("websocket is disconnected: \(e.localizedDescription)")
    } else {
      print("websocket disconnected")
    }
    self.isConnecting = false
    JsonRPCService.shared.removeIDs()
    idGenerator = JsonIdGenerator()
    batchFactory.idGenerator = idGenerator

    autoConnect()
  }
  
  func websocketDidReceiveMessage(socket: WebSocketClient, text: String) {
    let data = JSON(parseJSON:text)
    
    if let error = data["error"].dictionary {
      print(error)
      return
    }
    
    guard let id = data["id"].int else {
      if let method = data["method"].string, method == "notice", let params = data["params"].array, let mID = params[0].int {
        if let ids = UIApplication.shared.coordinator().state.property.subscribeIds, ids.contains(mID) {
          let index = ids.index(of: mID)
          UIApplication.shared.coordinator().request24hMarkets(index: index, sub: false)
        }
      }
      return
    }
    
    if let callback = callbacks[id] {
      callback(data.object)
      callbacks.removeValue(forKey: id)
    }
    
    print("Received text: \(data)")
  }
  
  func websocketDidReceiveData(socket: WebSocketClient, data: Data) {
  }
  
}
