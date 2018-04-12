//
//  Constants.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation

typealias CommonCallback = () -> Void
typealias CommonAnyCallback = (Any) -> Void

enum indicator:String {
  case none
  case macd = "MACD"
  case ema = "EMA"
  case ma = "MA"
  case boll = "BOLL"
  
  static let all:[indicator] = [.ma, .ema, .macd, .boll]
}

enum candlesticks:Double,Hashable {
  case five_minute = 300.0
  case one_hour = 3600.0
  case one_day = 86400.0
  
  static func ==(lhs: candlesticks, rhs: candlesticks) -> Bool {
    return lhs.rawValue == rhs.rawValue
  }
  
  var hashValue: Int {
    return self.rawValue.toInt
  }
  
  static let all:[candlesticks] = [.five_minute, .one_hour, .one_day]
}

struct Config {
   static let asset_ids = [[assetID.cyb.rawValue, assetID.eth.rawValue],
                           [assetID.cyb.rawValue, assetID.btc.rawValue],
                           [assetID.cyb.rawValue, assetID.omg.rawValue],
                           [assetID.cyb.rawValue, assetID.ven.rawValue],
                           [assetID.cyb.rawValue, assetID.bat.rawValue],
                           [assetID.cyb.rawValue, assetID.eos.rawValue],
                           [assetID.cyb.rawValue, assetID.snt.rawValue],
                           [assetID.cyb.rawValue, assetID.eng.rawValue],
                           [assetID.cyb.rawValue, assetID.knc.rawValue],
                           [assetID.cyb.rawValue, assetID.nas.rawValue],
                           [assetID.cyb.rawValue, assetID.pay.rawValue]]
}

enum objectID:String {
  case base_object = "1.1.x"
  case account_object = "1.2.x"
  case asset_object = "1.3.x"
  case force_settlement_object = "1.4.x"
  case committee_member_object = "1.5.x"
  case witness_object = "1.6.x"
  case limit_order_object = "1.7.x"
  case call_order_object = "1.8.x"
  case custom_object = "1.9.x"
  case proposal_object = "1.10.x"
  case operation_history_object = "1.11.x"
  case withdraw_permission_object = "1.12.x"
  case vesting_balance_object = "1.13.x"
  case worker_object = "1.14.x"
  case balance_object = "1.15.x"
  case global_property_object = "2.0.x"
  case dynamic_global_property_object = "2.1.x"
  case asset_dynamic_data = "2.3.x"
  case asset_bitasset_data = "2.4.x"
  case account_balance_object = "2.5.x"
  case account_statistics_object = "2.6.x"
  case transaction_object = "2.7.x"
  case block_summary_object = "2.8.x"
  case account_transaction_history_object = "2.9.x"
  case blinded_balance_object = "2.10.x"
  case chain_property_object = "2.11.x"
  case witness_schedule_object = "2.12.x"
  case budget_record_object = "2.13.x"
  case special_authority_object = "2.14.x"
}

enum assetID:String {
  
  case cyb = "1.3.0"
  case eth = "1.3.2"
  case btc = "1.3.3"
  case omg = "1.3.8"
  case ven = "1.3.7"
  case bat = "1.3.6"
  case eos = "1.3.4"
  case snt = "1.3.5"
  case eng = "1.3.12"
  case knc = "1.3.10"
  case nas = "1.3.9"
  case pay = "1.3.11"
  
  static let all:[assetID] = [.cyb, .eth, .btc, .omg, .ven, .bat, .eos, .snt, .eng, .knc, .nas, .pay]
  
  var assetIcon:String {
    switch self {
    case .cyb:
      return "ic_cyb_grey"
    case .eth:
      return "ic_eth_grey"
    case .btc:
      return "ic_btc_grey"
    case .omg:
      return "ic_omg_grey"
    case .ven:
      return "ic_ven_grey"
    case .bat:
      return "ic_bat_grey"
    case .eos:
      return "ic_eos_grey"
    case .snt:
      return "ic_snt_grey"
    case .eng:
      return "ic_eng_grey"
    case .knc:
      return "ic_knc_grey"
    case .nas:
      return "ic_nas_grey"
    case .pay:
      return "ic_pay_grey"
   
    }
  }
}

extension assetID:Equatable {
  static func ==(lhs: assetID, rhs: assetID) -> Bool {
    return lhs.rawValue == rhs.rawValue
  }
}

protocol ObjectDescriptable {
  func propertyDescription() -> String
}

extension ObjectDescriptable {
  func propertyDescription() -> String {
    let strings = Mirror(reflecting: self).children.flatMap { "\($0.label!): \($0.value)" }
    var string = ""
    for str in strings {
      string += String(str) + "\n"
    }
    return string
  }
}

