//
//  AppDelegate.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/9.
//  Copyright © 2018年 Facebook. All rights reserved.
//

import Foundation
import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
    self.window = UIWindow.init(frame: UIScreen.main.bounds)
    self.window?.backgroundColor = .white
    
    
//    let jsCodeLocation = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: "")
//    let rootView = RCTRootView(bundleURL: jsCodeLocation, moduleName: "cybexMobile", initialProperties: [:], launchOptions: launchOptions)
//
    configApplication()
  
    let rootVC = MainViewController()
    window?.rootViewController = rootVC
    self.window?.makeKeyAndVisible()
    
    return true
  }
  
  func applicationWillResignActive(_ application: UIApplication) {
  }
  
  func applicationDidEnterBackground(_ application: UIApplication) {
  }
  
  func applicationWillEnterForeground(_ application: UIApplication) {
  }
  
  func applicationDidBecomeActive(_ application: UIApplication) {
  }
  
  func applicationWillTerminate(_ application: UIApplication) {
  }
  
  func applicationDidReceiveMemoryWarning(_ application: UIApplication) {
  }
  
  func configApplication() {
//    ThemeManager.setTheme(plistName: "ThemeNormal", path: .mainBundle)
  }
}
