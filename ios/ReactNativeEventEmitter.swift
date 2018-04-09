//
//  ReactNativeEventEmitter.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/22.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation

import Foundation

@objc(ReactNativeEventEmitter)
open class ReactNativeEventEmitter: RCTEventEmitter {
  
  override init() {
    super.init()
    EventEmitter.shared.registerEventEmitter(eventEmitter: self)
  }
  
  /// Base overide for RCTEventEmitter.
  ///
  /// - Returns: all supported events
  @objc open override func supportedEvents() -> [String] {
    return EventEmitter.shared.allEvents
  }
  
}
