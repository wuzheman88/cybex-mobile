//
//  EventEmitter.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/22.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation

class EventEmitter {

  /// Shared Instance.
  public static var shared = EventEmitter()

  // ReactNativeEventEmitter is instantiated by React Native with the bridge.
  private static var eventEmitter: ReactNativeEventEmitter!

  private init() {}

  // When React Native instantiates the emitter it is registered here.
  func registerEventEmitter(eventEmitter: ReactNativeEventEmitter) {
    EventEmitter.eventEmitter = eventEmitter
  }

  func dispatch(name: String, body: Any?) {
    EventEmitter.eventEmitter.sendEvent(withName: name, body: body)
  }

  /// All Events which must be support by React Native.
  lazy var allEvents: [String] = {
    var allEventNames: [String] = []
    
    // Append all events here
    
    return allEventNames
  }()

}
