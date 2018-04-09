//
//  DataBaseApi.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import JSONRPCKit
import SwiftyJSON

enum dataBaseCatogery:String {
  case get_chain_id
  case get_objects
  case subscribe_to_market
  case get_limit_orders
}

struct GetChainIDRequest: JSONRPCKit.Request {
  typealias Response = String
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.database] ?? 0, dataBaseCatogery.get_chain_id.rawValue, []]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? Response {
      return response
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  }
}

struct GetAssetRequest: JSONRPCKit.Request {
  typealias Response = [AssetInfo]
  
  var ids:[String]
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.database] ?? 0, dataBaseCatogery.get_objects.rawValue, [ids]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? [[String: Any]] {
      return response.map { data in
        
        return try! AssetInfo(JSON:data)
      }
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  
  }
}

struct SubscribeMarketRequest: JSONRPCKit.Request {
  typealias Response = Any
  
  var ids:[String]
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.database] ?? 0, dataBaseCatogery.subscribe_to_market.rawValue, [NetWorkService.shared.idGenerator.currentId + 1, ids[0], ids[1]]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    return response
  }
}

struct getLimitOrdersRequest: JSONRPCKit.Request {
  typealias Response = Any
  
  var ids:[String]
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.database] ?? 0, dataBaseCatogery.get_limit_orders.rawValue, [ids[0], ids[1], 20]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    let result = JSON(resultObject).arrayValue
    if result.count > 1 {
      let half = result.count / 2
      let buy = result[0..<half]
      
      let sell = Array(result[half..<result.count])
      
      var data:[JSON] = []
      for i in 0..<half {
        let buy_data = buy[i]
        let sell_data = sell[i]
        let buy_price = buy_data["sell_price"]
        let sell_price = sell_data["sell_price"]
        data.append([buy_price, sell_price, buy_data["for_sale"], sell_data["for_sale"]])
      }
      
      return data
    }
    else {
      return []
    }
  }
}
