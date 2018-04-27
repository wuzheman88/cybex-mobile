//
//  Asset.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ObjectMapper

class Bucket : Mappable, NSCopying {
  var id: String = ""
  
  var base_volume: String = ""
  var quote_volume: String = ""
  
  var high_base: String = ""
  var high_quote: String = ""
  var low_base: String = ""
  var low_quote: String = ""
  
  var open_base: String = ""
  var open_quote: String = ""
  var close_base: String = ""
  var close_quote: String = ""
  
  var open:TimeInterval = 0
  var base:String = ""
  var quote:String = ""
  var seconds:String = ""


  required init?(map: Map) {
  }

  func mapping(map: Map) {
    id                   <- (map["id"],ToStringTransform())
    base_volume          <- (map["base_volume"],ToStringTransform())
    quote_volume         <- (map["quote_volume"],ToStringTransform())
    high_base            <- (map["high_base"],ToStringTransform())
    high_quote           <- (map["high_quote"],ToStringTransform())
    low_base             <- (map["low_base"],ToStringTransform())
    low_quote            <- (map["low_quote"],ToStringTransform())
    open_base            <- (map["open_base"],ToStringTransform())
    open_quote           <- (map["open_quote"],ToStringTransform())
    close_base           <- (map["close_base"],ToStringTransform())
    close_quote          <- (map["close_quote"],ToStringTransform())
    open                 <- (map["key.open"], DateIntervalTransform())
    base          <- (map["key.base"],ToStringTransform())
    quote          <- (map["key.quote"],ToStringTransform())
    seconds         <- (map["key.seconds"],ToStringTransform())
  }
  
  func copy(with zone: NSZone? = nil) -> Any {
    let copy = Bucket(JSON: self.toJSON())!
    return copy
  }
  
  static func empty() -> Bucket {
    return Bucket(JSON: [:])!
  }
  
}

enum changeScope {
  case greater
  case less
  case equal
  
  func icon() -> UIImage {
    switch self {
    case .greater:
      return #imageLiteral(resourceName: "ic_arrow_green.pdf")
    case .less:
      return #imageLiteral(resourceName: "ic_arrow_red.pdf")
    case .equal:
      return #imageLiteral(resourceName: "ic_arrow_grey2.pdf")
    }
  }
  
  func color() -> UIColor {
    switch self {
    case .greater:
      return #colorLiteral(red: 0.4922918081, green: 0.7674361467, blue: 0.356476903, alpha: 1)
    case .less:
      return #colorLiteral(red: 0.7984321713, green: 0.3588138223, blue: 0.2628142834, alpha: 1)
    case .equal:
      return #colorLiteral(red: 0.9999966025, green: 0.9999999404, blue: 0.9999999404, alpha: 0.5)
    }
  }
}

class BucketMatrix {
  var price:String = ""
  
  var asset:[Bucket]
  
  var base_volume_origin:Double = 0

  var base_volume:String = ""
  var quote_volume:String = ""
  
  var high:String = ""
  var low:String = ""
  
  var change:String = ""
  
  var incre:changeScope = .equal
  

  init(_ homebucket:HomeBucket) {
    self.asset = homebucket.bucket
    
    let last = self.asset.last!
    let first = self.asset.first!

    let flip = homebucket.base != last.base

    let last_closebase_amount = flip ? Double(last.close_quote)! : Double(last.close_base)!
    let last_closequote_amount = flip ? Double(last.close_base)! : Double(last.close_quote)!

    let first_openbase_amount = flip ? Double(first.open_quote)! : Double(first.open_base)!
    let first_openquote_amount = flip ? Double(first.open_base)! : Double(first.open_quote)!


    let base_info = homebucket.base_info
    let quote_info = homebucket.quote_info

    let base_precision = pow(10, base_info.precision.toDouble)
    let quote_precision = pow(10, quote_info.precision.toDouble)

    let lastClose_price = (last_closebase_amount / base_precision) / (last_closequote_amount / quote_precision)
    let firseOpen_price = (first_openbase_amount / base_precision) / (first_openquote_amount / quote_precision)


    let high_price_collection = self.asset.map { (bucket) -> Double in
      let high_base = flip ? (Double(bucket.high_quote)! / base_precision) : (Double(bucket.high_base)! / base_precision)
      let quote_base = flip ? (Double(bucket.high_base)! / quote_precision) : (Double(bucket.high_quote)! / quote_precision)
      return high_base / quote_base
    }

    let low_price_collection = self.asset.map { (bucket) -> Double in
      let low_base = flip ? (Double(bucket.low_quote)! / base_precision) : (Double(bucket.low_base)! / base_precision)
      let low_quote = flip ? (Double(bucket.low_base)! / quote_precision) : (Double(bucket.low_quote)! / quote_precision)
      return low_base / low_quote
    }

    let high = high_price_collection.max()!
    let low = low_price_collection.min()!

    let now = Date().addingTimeInterval(-24 * 3600).timeIntervalSince1970

    let base_volume = self.asset.filter({$0.open > now}).map{flip ? $0.quote_volume : $0.base_volume}.reduce(0) { (last, cur) -> Double in
      last + Double(cur)!
    } / base_precision

    let quote_volume = self.asset.filter({$0.open > now}).map{flip ? $0.base_volume: $0.quote_volume}.reduce(0) { (last, cur) -> Double in
      last + Double(cur)!
      } / quote_precision


    self.base_volume_origin = base_volume
    self.base_volume = base_volume.toString.suffixNumber()
    self.quote_volume = quote_volume.toString.suffixNumber()

    self.high = high.toString.formatCurrency(digitNum: base_info.precision)
    self.low = low.toString.formatCurrency(digitNum: base_info.precision)

    let isCYB = homebucket.base == AssetConfiguration.CYB
    self.price = lastClose_price.toString.formatCurrency(digitNum: isCYB ? 5 : 8)

    let change = (lastClose_price - firseOpen_price) * 100 / firseOpen_price
    let percent = round(change * 100) / 100.0

    self.change = percent.toString.formatCurrency(digitNum: 2)

    if percent == 0 {
      self.incre = .equal
    }
    else if percent < 0 {
      self.incre = .less
    }
    else {
      self.incre = .greater
    }
    
  }
  
}

class DateIntervalTransform: TransformType {
  public typealias Object = Double
  public typealias JSON = String
  
  public init() {}
  
  open func transformFromJSON(_ value: Any?) -> Double? {
    if let time = value as? String {
      return time.dateFromISO8601?.timeIntervalSince1970
    }
    
    return nil
  }
  
  open func transformToJSON(_ value: Double?) -> String? {
    if let date = value {
      let d = Date(timeIntervalSince1970: date)
      return d.iso8601
    }
    return nil
  }
}

class ToStringTransform: TransformType {
  public typealias Object = String
  public typealias JSON = String
  
  public init() {}
  
  open func transformFromJSON(_ value: Any?) -> String? {
    if let v = value as? Double {
      return String(describing: v)
    }
    else if let v = value as? String {
      return v
    }
    else if let v = value as? Int {
      return v.toString
    }
    
    return nil
  }
  
  open func transformToJSON(_ value: String?) -> String? {
    if let v = value {
      return v
    }
    return nil
  }
}


extension Bucket: Equatable {
  static func ==(lhs: Bucket, rhs: Bucket) -> Bool {
    return lhs.id == rhs.id && lhs.base_volume == rhs.base_volume && lhs.quote_volume == rhs.quote_volume && lhs.high_base == rhs.high_base && lhs.high_quote == rhs.high_quote && lhs.low_base == rhs.low_base && lhs.low_quote == rhs.low_quote && lhs.open_base == rhs.open_base && lhs.open_quote == rhs.open_quote && lhs.close_base == rhs.close_base && lhs.close_quote == rhs.close_quote && lhs.open == rhs.open && lhs.base == rhs.base && lhs.quote == rhs.quote && lhs.seconds == rhs.seconds
  }
}
