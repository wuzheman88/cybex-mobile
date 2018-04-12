//
//  AppDelegate.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/9.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import UIKit

import Localize_Swift
import SwiftTheme
import RealReachability
import Peek
import SwiftyUserDefaults

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  var appCoordinator: AppCoordinator!
  var enteredBackground:Bool = false

  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
    self.window = UIWindow.init(frame: UIScreen.main.bounds)
    self.window?.theme_backgroundColor = [#colorLiteral(red: 0.1178231761, green: 0.1536857784, blue: 0.2179759443, alpha: 1).hexString(true), #colorLiteral(red: 0.9750029445, green: 0.9783667922, blue: 0.9844790101, alpha: 1).hexString(true)]

    self.window?.backgroundColor = ThemeManager.currentThemeIndex == 0 ? #colorLiteral(red: 0.1178231761, green: 0.1536857784, blue: 0.2179759443, alpha: 1) : #colorLiteral(red: 0.9750029445, green: 0.9783667922, blue: 0.9844790101, alpha: 1)
    
    window?.peek.enabled = true
    
//    let jsCodeLocation = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: "")
//    let rootView = RCTRootView(bundleURL: jsCodeLocation, moduleName: "cybexMobile", initialProperties: [:], launchOptions: launchOptions)
  
    let rootVC = BaseTabbarViewController()
    window?.rootViewController = rootVC
    self.window?.makeKeyAndVisible()
    
    appCoordinator = AppCoordinator(rootVC: rootVC)
    appCoordinator.start()
    
  
    RealReachability.sharedInstance().startNotifier()
    NotificationCenter.default.addObserver(forName: NSNotification.Name.realReachabilityChanged, object: self, queue: nil) { (notifi) in
      NetWorkService.shared.checkNetworAndConnect()
    }
    
    configApplication()

    return true
  }
  
  func applicationWillResignActive(_ application: UIApplication) {
  }
  
  func applicationDidEnterBackground(_ application: UIApplication) {
    self.enteredBackground = true
  }
  
  func applicationWillEnterForeground(_ application: UIApplication) {
  }
  
  func applicationDidBecomeActive(_ application: UIApplication) {
    if self.enteredBackground {
      NetWorkService.shared.checkNetworAndConnect()
    }
    self.enteredBackground = false
  }
  
  func applicationWillTerminate(_ application: UIApplication) {
  }
  
  func applicationDidReceiveMemoryWarning(_ application: UIApplication) {
  }
  
  func configApplication() {
    UIApplication.shared.theme_setStatusBarStyle([.lightContent, .default], animated: true)

    if !Defaults.hasKey(.theme) {
      ThemeManager.setTheme(index: 0)
    }
    else {
      ThemeManager.setTheme(index: Defaults[.theme])
    }
   
    if !Defaults.hasKey(.language) {
      Localize.setCurrentLanguage("en")
    }
    else {
      Localize.setCurrentLanguage(Defaults[.language])
    }
    
    _ = NetWorkService.shared
  }
}
