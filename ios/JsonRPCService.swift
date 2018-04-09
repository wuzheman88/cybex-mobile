//
//  JsonRPCService.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import JSONRPCKit

enum apiCategory:String {
  case login
  case database
  case network_broadcast
  case history
}

struct CastError<ExpectedType>: Error {
  let actualValue: Any
  let expectedType: ExpectedType.Type
}

public struct JsonIdGenerator: IdGenerator {
  
  var currentId = 1
  
  public init() {}
  
  public mutating func next() -> Id {
    defer {
      currentId += 1
    }
    
    return .number(currentId)
  }
}

class JsonRPCService {
  typealias RPCResponse = ([Any?]) -> ()

  var ids:[apiCategory:Int] = [:]
  
  var retryRequests:[(String, Any, RPCResponse?)] = []
  
  var allrequestRetry:[()->()] = []
  var isFetchingID:Bool = false
  
  private init() {
   
  }
  
  func requestIDs<Request: JSONRPCKit.Request>(_ request:[Request] = []) {
    let retry:()->() = {
      for operation in self.retryRequests where self.ids.count == 3 {
        if  let request = operation.1 as? [Request] {
          NetWorkService.shared.send(request: request, callback: operation.2)
          let digests = self.retryRequests.map { $0.0 }
          
          let params = request.flatMap({ (request) -> [Any] in
            var data = request.parameters as! [Any]
            data.removeFirst()
            return data
          })
          
          
          let digest = params.reduce("", { (last, cur) -> String in
            return last + "\(cur)"
          })
          
          if let index = digests.index(of: digest) {
            self.retryRequests.remove(at: index)
          }
        }
      }
    }
    self.allrequestRetry.append(retry)
    
    guard !JsonRPCService.shared.isFetchingID else {
      
      return
    }

    self.isFetchingID = true
    
    NetWorkService.shared.send(false, request: [LoginRequest(username: "", password: "")]) {_ in}
    NetWorkService.shared.send(false, request: [RegisterIDRequest(api: .database), RegisterIDRequest(api: .network_broadcast), RegisterIDRequest(api: .history)]) { response in
      if let databaseID = response[0] as? Int, let broad = response[1] as? Int, let history = response[2] as? Int {
        self.ids[.database] = databaseID
        self.ids[.network_broadcast] = broad
        self.ids[.history] = history
        self.isFetchingID = false
     
        for retry in self.allrequestRetry {
          retry()
        }
      }
    }
    
  }
  
  func addRetryRequest(_ request: (String, Any, RPCResponse?)) {
    let digests = self.retryRequests.map { $0.0 }
    if digests.contains(request.0) {
      return
    }
    self.retryRequests.append(request)
  }
  
  func existAllIDs() -> Bool {
    return self.ids.count == 3
  }
  
  func removeIDs() {
    self.ids.removeAll()
    self.retryRequests.removeAll()
  }
  
  static let shared = JsonRPCService()

}

extension JSONRPCKit.Request {
  var method: String {
    return "call"
  }
}

struct RegisterIDRequest: JSONRPCKit.Request {
  typealias Response = Int
  
  var api:apiCategory
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [1, api.rawValue, []]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? Response {
      return response
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  }
}

struct LoginRequest: JSONRPCKit.Request {
  typealias Response = Bool
  
  let username:String
  let password:String
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [1, apiCategory.login.rawValue, [username, password]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? Response {
      return response
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  }
}

struct LoginRequest2: JSONRPCKit.Request {
  typealias Response = Bool
  
  let username:String
  let password:String
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [1, apiCategory.login.rawValue, [username, password]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? Response {
      return response
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  }
}
