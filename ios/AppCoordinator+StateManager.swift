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
    store.dispatch(creator.fetchAssets(with: sub, params:params, callback: {[weak self] (index, assets) in
      guard let `self` = self else { return }
      
      self.store.dispatch(AssetsFetched(index: index, assets: assets))
      self.store.dispatch(FetchOver())
    }))
  }
  
  func fetchKline(_ params:AssetPairQueryParams, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    store.dispatch(creator.fetchAssets(with: false, params:params, callback: {[weak self] (index, assets) in
      guard let `self` = self else { return }

      self.store.dispatch(kLineFetched(index: index, stick: gap, assets: assets))
      self.store.dispatch(FetchKlineOver())
      if let vc = vc, let sel = selector {
        self.store.dispatch(RefreshState(sel: sel, vc: vc))
      }
    }))
  }
  
  func fetchAsset(_ ids: [assetID]) {
    let request = GetAssetRequest(ids: ids.map { $0.rawValue} )
    NetWorkService.shared.send(request: [request]) { response in
      if let assetinfo = response[0] as? [AssetInfo] {
        for info in assetinfo {
          self.store.dispatch(AssetInfoAction(assetID: assetID(rawValue:info.id)!, info: info))
        }
      }
    }
  }
  
}

extension AppCoordinator {
  func request24hMarkets(index:Int? = nil, sub:Bool = true) {
    let now = Date()
    var start = now.addingTimeInterval(-3600*24)
    
    let timePassed = (-start.minute * 60 - start.second).toDouble
    start = start.addingTimeInterval(timePassed)
    
    if let index = index {
      let pair = Config.asset_ids[index]
      UIApplication.shared.coordinator().fetchData(AssetPairQueryParams(firstAssetId: pair[0], secondAssetId: pair[1], timeGap: 60 * 60, startTime: start, endTime: now), sub:sub)
      return
    }
    
    for pair in Config.asset_ids {

      UIApplication.shared.coordinator().fetchData(AssetPairQueryParams(firstAssetId: pair[0], secondAssetId: pair[1], timeGap: 60 * 60, startTime: start, endTime: now), sub: sub)
    }
   
  }
  
  func requestKlineDetailData(index:Int, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    let now = Date()
    let start = now.addingTimeInterval(-gap.rawValue * 199)
    
    let pair = Config.asset_ids[index]
    UIApplication.shared.coordinator().fetchKline(AssetPairQueryParams(firstAssetId: pair[0], secondAssetId: pair[1], timeGap: gap.rawValue.toInt, startTime: start, endTime: now), gap:gap, vc:vc, selector: selector)
  }
  
  
  func getLatestData() {
    request24hMarkets()
  }
}



