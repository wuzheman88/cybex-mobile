//
//  AssetInfo.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/27.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ObjectMapper

class AssetInfo : ImmutableMappable {
  var precision: Int
  var id: String
  var symbol: String
  var dynamic_asset_data_id: String


  required  init(map: Map) throws {
    precision            = try map.value("precision")
    id                   = try map.value("id")
    symbol               = try map.value("symbol")
    dynamic_asset_data_id = try map.value("dynamic_asset_data_id")
  }

  func mapping(map: Map) {
    precision            >>> map["precision"]
    id                   >>> map["id"]
    symbol               >>> map["symbol"]
    dynamic_asset_data_id >>> map["dynamic_asset_data_id"]
  }
}

class Asset : ImmutableMappable {
  let amount: String
  let assetID: assetID
  
  required  init(map: Map) throws {
    amount               = try map.value("amount", using:ToStringTransform())
    assetID              = try map.value("asset_id")
  }
  
  func mapping(map: Map) {
    amount               >>> (map["amount"], ToStringTransform())
    assetID              >>> map["asset_id"]
  }
  
  func volume() -> Double {
    let info = UIApplication.shared.coordinator().state.property.assetInfo[assetID]!
    
    return amount.toDouble()! / pow(10, info.precision.toDouble)
  }
  
  func info() -> AssetInfo {
    return UIApplication.shared.coordinator().state.property.assetInfo[self.assetID]!
  }
}

extension Asset: Equatable {
  static func ==(lhs: Asset, rhs: Asset) -> Bool {
    return lhs.assetID == rhs.assetID
  }
}

class Price : ImmutableMappable {
  let base:Asset
  let quote:Asset
  
  required  init(map: Map) throws {
    base                    = try map.value("base")
    quote                   = try map.value("quote")
  }
  
  func mapping(map: Map) {
    base                    >>> map["base"]
    quote                   >>> map["quote"]
  }
  
  func toReal() -> Double {
    let base_info = base.info()
    let quote_info = quote.info()
    
    let price_ratio =  base.amount.toDouble()! / quote.amount.toDouble()!
    let precision_ratio = pow(10, base_info.precision.toDouble) / pow(10, quote_info.precision.toDouble)
    
    return price_ratio / precision_ratio
  }
  
}

extension AssetInfo: Equatable {
  static func ==(lhs: AssetInfo, rhs: AssetInfo) -> Bool {
    return lhs.precision == rhs.precision && lhs.id == rhs.id && lhs.symbol == rhs.symbol && lhs.dynamic_asset_data_id == rhs.dynamic_asset_data_id
  }
}

