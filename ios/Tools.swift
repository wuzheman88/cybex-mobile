//
//  Tools.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation

func prettyPrint(with json: [String:Any]) -> String {
  let data = try! JSONSerialization.data(withJSONObject: json, options: .prettyPrinted)
  let string = String(data: data, encoding: .utf8)!
  return string
}

extension NSLayoutConstraint {
  func changeMultiplier(multiplier: CGFloat) -> NSLayoutConstraint {
    let newConstraint = NSLayoutConstraint(
      item: firstItem as Any,
      attribute: firstAttribute,
      relatedBy: relation,
      toItem: secondItem,
      attribute: secondAttribute,
      multiplier: multiplier,
      constant: constant)
    newConstraint.priority = priority
    
    NSLayoutConstraint.deactivate([self])
    NSLayoutConstraint.activate([newConstraint])
    
    return newConstraint
  }
  
}
extension Formatter {
  static let iso8601: DateFormatter = {
    let formatter = DateFormatter()
    formatter.calendar = Calendar(identifier: .iso8601)
    formatter.locale = Locale(identifier: "en_US_POSIX")
    formatter.timeZone = TimeZone(secondsFromGMT: 0)
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    return formatter
  }()
}
extension Date {
  var iso8601: String {
    return Formatter.iso8601.string(from: self)
  }
}

extension String {
  var dateFromISO8601: Date? {
    return Formatter.iso8601.date(from: self)   // "Mar 22, 2017, 10:22 AM"
  }
  
  func formatCurrency(digitNum:Int) -> String {
    let formatter = NumberFormatter()
    formatter.numberStyle = .currency
    formatter.currencySymbol = ""
    formatter.maximumFractionDigits = digitNum
    formatter.minimumFractionDigits = 0

    let result = formatter.string(from: NumberFormatter().number(from: self)!)
    return result!
  }
  
  func suffixNumber(digitNum:Int = 5) -> String {
    var num:Double = self.toDouble()!
    let sign = ((num < 0) ? "-" : "" )
    num = fabs(num)
    if (num < 1000.0) {
      return "\(sign)\(num.toString.formatCurrency(digitNum: digitNum))"
    }
    
    let exp: Int = Int(log10(num) / 3.0)
    let units: [String] = ["k","m","b"]
    let roundedNum: Double = round(100 * num / pow(1000.0,Double(exp))) / 100
    
    return "\(sign)\(roundedNum)" + "\(units[exp-1])"
  }
}


