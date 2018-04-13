//
//  AppCoordinator+StateManager.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/26.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift

extension AppCoordinator:AppStateManagerProtocol {
  func subscribe<SelectedState, S: StoreSubscriber>(
    _ subscriber: S, transform: ((Subscription<AppState>) -> Subscription<SelectedState>)?
    ) where S.StoreSubscriberStateType == SelectedState {
    store.subscribe(subscriber, transform: transform)
  }
  
  func fetchData(_ params:AssetPairQueryParams, sub:Bool = true) {
    store.dispatch(creator.fetchAssets(with: sub, params:params, callback: {[weak self] (assets) in
      guard let `self` = self else { return }
     
      self.store.dispatch(AssetsFetched(pair: params, assets: assets))
      self.store.dispatch(FetchOver())
    }))
  }
  
  func fetchKline(_ params:AssetPairQueryParams, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    store.dispatch(creator.fetchAssets(with: false, params:params, callback: {[weak self] (assets) in
      guard let `self` = self else { return }

      self.store.dispatch(kLineFetched(assetID:params.secondAssetId, stick: gap, assets: assets))
      self.store.dispatch(FetchKlineOver())
      if let vc = vc, let sel = selector {
        self.store.dispatch(RefreshState(sel: sel, vc: vc))
      }
    }))
  }
  
  func fetchAsset() {
    guard AssetConfiguration.shared.asset_ids.count > 0 else { return }
    
    let request = GetAssetRequest(ids: AssetConfiguration.shared.asset_ids + [AssetConfiguration.CYB])
    NetWorkService.shared.send(request: [request]) { response in
      if let assetinfo = response[0] as? [AssetInfo] {
        for info in assetinfo {
          self.store.dispatch(AssetInfoAction(assetID: info.id, info: info))
        }
      }
    }
  }
  
}

extension AppCoordinator {
  func request24hMarkets(specialID:String? = nil, sub:Bool = true) {
    let now = Date()
    var start = now.addingTimeInterval(-3600*24)
    
    let timePassed = (-start.minute * 60 - start.second).toDouble
    start = start.addingTimeInterval(timePassed)
    

    if let special = specialID {
      UIApplication.shared.coordinator().fetchData(AssetPairQueryParams(firstAssetId: AssetConfiguration.CYB, secondAssetId: special, timeGap: 60 * 60, startTime: start, endTime: now), sub:sub)
      return
    }
    
    for assetID in AssetConfiguration.shared.asset_ids {

      UIApplication.shared.coordinator().fetchData(AssetPairQueryParams(firstAssetId: AssetConfiguration.CYB, secondAssetId: assetID, timeGap: 60 * 60, startTime: start, endTime: now), sub: sub)
    }
   
  }
  
  func requestKlineDetailData(sepcialID:String, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    let now = Date()
    let start = now.addingTimeInterval(-gap.rawValue * 199)
    
    UIApplication.shared.coordinator().fetchKline(AssetPairQueryParams(firstAssetId: AssetConfiguration.CYB, secondAssetId: sepcialID, timeGap: gap.rawValue.toInt, startTime: start, endTime: now), gap:gap, vc:vc, selector: selector)
  }
  
  
  func getLatestData() {
    request24hMarkets()
  }
}



