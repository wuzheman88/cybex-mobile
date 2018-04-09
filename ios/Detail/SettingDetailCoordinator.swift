//
//  SettingDetailCoordinator.swift
//  cybexMobile
//
//  Created koofrank on 2018/4/2.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import UIKit
import ReSwift

protocol SettingDetailCoordinatorProtocol {
  func popViewController(_ animated: Bool)
}

protocol SettingDetailStateManagerProtocol {
    var state: SettingDetailState { get }
    func subscribe<SelectedState, S: StoreSubscriber>(
        _ subscriber: S, transform: ((Subscription<SettingDetailState>) -> Subscription<SelectedState>)?
    ) where S.StoreSubscriberStateType == SelectedState
}

class SettingDetailCoordinator: SettingRootCoordinator {
    
    lazy var creator = SettingDetailPropertyActionCreate(vc: self.rootVC.topViewController as? BaseViewController)
    
    var store = Store<SettingDetailState>(
        reducer: SettingDetailReducer,
        state: nil,
        middleware:[TrackingMiddleware]
    )
}

extension SettingDetailCoordinator: SettingDetailCoordinatorProtocol {
  func popViewController(_ animated: Bool) {
    self.rootVC.popViewController(animated: animated)
  }
}

extension SettingDetailCoordinator: SettingDetailStateManagerProtocol {
    var state: SettingDetailState {
        return store.state
    }
    
    func subscribe<SelectedState, S: StoreSubscriber>(
        _ subscriber: S, transform: ((Subscription<SettingDetailState>) -> Subscription<SelectedState>)?
        ) where S.StoreSubscriberStateType == SelectedState {
        store.subscribe(subscriber, transform: transform)
    }
    
}
