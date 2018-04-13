//
//  AppCoordinator.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ESTabBarController_swift
import Localize_Swift
import ReSwift
import SwiftTheme

protocol AppStateManagerProtocol {
  var state: AppState { get }
  func subscribe<SelectedState, S: StoreSubscriber>(
    _ subscriber: S, transform: ((Subscription<AppState>) -> Subscription<SelectedState>)?
  ) where S.StoreSubscriberStateType == SelectedState
  
  func fetchData(_ params:AssetPairQueryParams, sub:Bool)
}

class AppCoordinator {
  lazy var creator = AppPropertyActionCreate(vc: nil)
  
  var store = Store<AppState> (
    reducer: AppReducer,
    state: nil,
    middleware:[TrackingMiddleware]
  )
  
  var state: AppState {
    return store.state
  }
  
  var rootVC: BaseTabbarViewController
  
  var homeCoordinator: HomeRootCoordinator!
  var explorerCoordinator: ExplorerRootCoordinator!
  var faqCoordinator: FAQRootCoordinator!
  var settingCoordinator: SettingRootCoordinator!
  
  init(rootVC: BaseTabbarViewController) {
    self.rootVC = rootVC
  }
  
  func start() {
    if let tabBar = rootVC.tabBar as? ESTabBar {
      tabBar.barTintColor = #colorLiteral(red: 0.1178231761, green: 0.1536857784, blue: 0.2179759443, alpha: 1)
      tabBar.backgroundImage = UIImage()
    }
    
    let home = BaseNavigationController()
    homeCoordinator = HomeRootCoordinator(rootVC: home)
    home.tabBarItem = ESTabBarItem.init(CBTabBarView(), title: R.string.localizable.navWatchlist.key.localized(), image: R.image.ic_watchlist_24px(), selectedImage: R.image.ic_watchlist_active_24px())
    
    let faq = BaseNavigationController()
    faqCoordinator = FAQRootCoordinator(rootVC: faq)
    faq.tabBarItem = ESTabBarItem.init(CBTabBarView(), title: R.string.localizable.navApply.key.localized(), image: R.image.icon_apply(), selectedImage: R.image.icon_apply_active())
    
    let setting = BaseNavigationController()
    settingCoordinator = SettingCoordinator(rootVC: setting)
    setting.tabBarItem = ESTabBarItem.init(CBTabBarView(), title: R.string.localizable.navSetting.key.localized(), image: R.image.ic_settings_24px(), selectedImage: R.image.ic_settings_active_24px())
    
  
    //        home.tabBarItem.badgeValue = ""
    //        message.tabBarItem.badgeValue = "99+"
    
    homeCoordinator.start()
    faqCoordinator.start()
    settingCoordinator.start()
    
    rootVC.viewControllers = [home, faq, setting]
   
    NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: ThemeUpdateNotification), object: nil, queue: nil, using: { [weak self] notification in
      guard let `self` = self else { return }
     
      CBConfiguration.sharedConfiguration.themeIndex = ThemeManager.currentThemeIndex
    })
  }
  
  func curDisplayingCoordinator() -> NavCoordinator {
    let container = [homeCoordinator, faqCoordinator, settingCoordinator] as [NavCoordinator]
    return container[self.rootVC.selectedIndex]
  }
}

extension UIApplication {
  func coordinator() -> AppCoordinator {
    guard let d = self.delegate as? AppDelegate else { fatalError("app delegate name not match")}
    return d.appCoordinator
  }
  
  func globalState() -> AppState {
    return self.coordinator().store.state
  }
  
}
