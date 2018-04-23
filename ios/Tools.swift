//
//  Tools.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/21.
//  Copyright © 2018年 Cybex. All rights reserved.
//
import UIKit
import Foundation
import Alamofire
import SwiftyJSON
import SafariServices
import StoreKit
import SDCAlertView

public struct Version : Equatable, Comparable {
  public let major  : Int
  public let minor  : Int
  public let patch  : Int
  public let string : String?
  
  public init?(_ version: String) {
    
    let parts: Array<String> = version.split { $0 == "." }.map { String($0) }
    
    if let majorOptional = parts[optional:0], let minorOptional = parts[optional:1], let patchOptional = parts[optional:2],
      let majorInt = Int(majorOptional), let minorInt = Int(minorOptional), let patchInt = Int(patchOptional) {
      self.major = majorInt
      self.minor = minorInt
      self.patch = patchInt
      string = version
    } else {
      return nil
    }
  }
  
  public static func < (lhs: Version, rhs: Version) -> Bool {
    if lhs.major < rhs.major {
      return true
    }
    
    if lhs.minor < rhs.minor {
      return true
    }

    if lhs.patch < rhs.patch {
      return true
    }
    
    return false
  }

}

func requestMarketList(_ completion: @escaping (_ pairs: [Pair]) -> Void) {
  var request = URLRequest(url: URL(string: AppConfiguration.SERVER_MARKETLIST_URLString)!)
  request.cachePolicy = .reloadIgnoringCacheData
  
  Alamofire.request(request).responseJSON { (response) in
    guard let value = response.result.value else {
      completion([])
      return
    }
    
    let data = JSON(value).dictionaryValue["data"]!
    let pairs = data.arrayValue.map({ Pair(base: $0.arrayValue[0].stringValue, quote:$0.arrayValue[1].stringValue)})
    completion(pairs)
  }
}

func checkVersion(_ completion: @escaping (_ update:Bool, _ url:String, _ force:Bool) -> Void) {
  var request = URLRequest(url: URL(string: AppConfiguration.SERVER_VERSION_URLString)!)
  request.cachePolicy = .reloadIgnoringCacheData
  
  Alamofire.request(request).responseJSON { (response) in
    guard let value = response.result.value else {
      completion(false, "", false)
      return
    }
    
    let json = JSON(value)
    
    let lastest_version = json["version"].stringValue
    
    if let cur = Version(Bundle.main.version), let remote = Version(lastest_version) {
      if cur >= remote {
        completion(false, "", false)
        
        return
      }
      
      let force_data = json["force"]
      
      completion(true, json["url"].stringValue, force_data[Bundle.main.version].boolValue)
      return
    }
    
    completion(false, "", false)

  }
}


func prettyPrint(with json: [String:Any]) -> String {
  let data = try! JSONSerialization.data(withJSONObject: json, options: .prettyPrinted)
  let string = String(data: data, encoding: .utf8)!
  return string
}

extension UIViewController {
  func openStoreProductWithiTunesItemIdentifier(_ identifier: String) {
    let storeViewController = SKStoreProductViewController()
    storeViewController.delegate = self
    
    let parameters = [ SKStoreProductParameterITunesItemIdentifier : identifier]
    storeViewController.loadProduct(withParameters: parameters) { [weak self] (loaded, error) -> Void in
      if loaded {
        guard let `self` = self else { return }
        
        self.present(storeViewController, animated: true)
      }
    }
  }
  
  func openSafariViewController(_ urlString:String) {
    if let url = URL(string: urlString) {
      let vc = SFSafariViewController(url: url, entersReaderIfAvailable: true)
      vc.delegate = self
      
      self.present(vc, animated: true)
    }
  }

  
  func handlerUpdateVersion(_ completion:CommonCallback?, showNoUpdate:Bool = false) {
    checkVersion { (update, url, force) in
      if let completion = completion {
        completion()
      }
      if update {
        let alert = AlertController(title: "Update Available", message: "A new version of CybexDex is available. Please update to newest version now", preferredStyle: .alert)
        
        if !force {
          alert.addAction(AlertAction(title: "Next Time", style: .normal, handler: nil))
        }
        else {
          alert.shouldDismissHandler = { (action) in
            if action?.title == "Next Time" {
              return true
            }
            else {
              action!.handler!(action!)
              return false
            }
          }
        }
        
        let action = AlertAction(title: "Update", style: .preferred, handler: { (action) in
          if force {
            UIApplication.shared.openURL(URL(string: url)!)
            return
          }
          if url.contains("itunes") {
            self.openStoreProductWithiTunesItemIdentifier(AppConfiguration.APPID)
          }
          else {
            self.openSafariViewController(url)
          }
        })
        alert.addAction(action)
        
        alert.present()
      }
      else if showNoUpdate {
        let alert = AlertController(title: "No Update Available", message: "Current Version is the newest", preferredStyle: .alert)
        alert.addAction(AlertAction(title: "OK", style: .normal, handler: nil))
        alert.present()
      }
   
    }
    
  }
}

extension UIViewController:SKStoreProductViewControllerDelegate {
  public func productViewControllerDidFinish(_ viewController: SKStoreProductViewController) {
    dismiss(animated: true)
  }
}

extension UIViewController:SFSafariViewControllerDelegate {
  public func safariViewControllerDidFinish(_ controller: SFSafariViewController) {
    dismiss(animated: true)
  }
}

extension Bundle {
  var version:String {
    return infoDictionary!["CFBundleShortVersionString"] as! String
  }
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
    formatter.minimumFractionDigits = digitNum

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
    
    return "\(sign)\(roundedNum.toString.formatCurrency(digitNum:2))" + "\(units[exp-1])"
  }
}


