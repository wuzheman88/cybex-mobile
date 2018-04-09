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
    precision            <- map["precision"]
    id                   <- map["id"]
    symbol               <- map["symbol"]
    dynamic_asset_data_id <- map["dynamic_asset_data_id"]
  }
}


extension AssetInfo: Equatable {
  static func ==(lhs: AssetInfo, rhs: AssetInfo) -> Bool {
    return lhs.precision == rhs.precision && lhs.id == rhs.id && lhs.symbol == rhs.symbol && lhs.dynamic_asset_data_id == rhs.dynamic_asset_data_id
  }
}
