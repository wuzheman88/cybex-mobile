//
//  HistoryApi.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import JSONRPCKit
import SwiftyJSON

enum historyCatogery:String {
  case get_market_history
  case get_fill_order_history
}

struct AssetPairQueryParams {
  var firstAssetId:String
  var secondAssetId:String
  var timeGap:Int
  var startTime:Date
  var endTime:Date
}

struct GetMarketHistoryRequest: JSONRPCKit.Request {
  typealias Response = [Bucket]
  
  var queryParams:AssetPairQueryParams
  

  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.history] ?? 0, historyCatogery.get_market_history.rawValue, [queryParams.firstAssetId, queryParams.secondAssetId, queryParams.timeGap, queryParams.startTime.iso8601, queryParams.endTime.iso8601]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    if let response = resultObject as? [[String: Any]] {
      return response.map { data in
    
        return Bucket(JSON:data)!
      }
    } else {
      throw CastError(actualValue: resultObject, expectedType: Response.self)
    }
  }
}


struct GetFillOrderHistoryRequest: JSONRPCKit.Request {
  typealias Response = [JSON]
  
  var firstAssetId:String
  var secondAssetId:String
  
  var method: String {
    return "call"
  }
  
  var parameters: Any? {
    return [JsonRPCService.shared.ids[apiCategory.history] ?? 0, historyCatogery.get_fill_order_history.rawValue, [firstAssetId, secondAssetId, 40]]
  }
  
  func response(from resultObject: Any) throws -> Response {
    let result = JSON(resultObject).arrayValue
   
    var data:[JSON] = []

    for re in result {
      data.append([re["op"]["pays"], re["op"]["receives"], re["time"]])
    }
    return data
  }
}
