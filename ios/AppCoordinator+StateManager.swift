//
//  AppCoordinator+StateManager.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/26.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift
import AwaitKit

extension AppCoordinator:AppStateManagerProtocol {
  func subscribe<SelectedState, S: StoreSubscriber>(
    _ subscriber: S, transform: ((Subscription<AppState>) -> Subscription<SelectedState>)?
    ) where S.StoreSubscriberStateType == SelectedState {
    store.subscribe(subscriber, transform: transform)
  }
  
  func fetchData(_ params:AssetPairQueryParams, sub:Bool = true) {
    store.dispatch(creator.fetchAssets(with: sub, params:params, callback: {[weak self] (assets) in
      guard let `self` = self else { return }
     
      self.store.dispatch(MarketsFetched(pair: params, assets: assets))
    }))
  }
  
  func fetchKline(_ params:AssetPairQueryParams, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    store.dispatch(creator.fetchAssets(with: false, params:params, callback: {[weak self] (assets) in
      guard let `self` = self else { return }

      self.store.dispatch(kLineFetched(pair:Pair(base: params.firstAssetId, quote: params.secondAssetId), stick: gap, assets: assets))
      if let vc = vc, let sel = selector {
        self.store.dispatch(RefreshState(sel: sel, vc: vc))
      }
    }))
  }
  
  func fetchAsset() {
    guard AssetConfiguration.shared.asset_ids.count > 0 else { return }
    
    let request = GetAssetRequest(ids: AssetConfiguration.shared.unique_ids)
    WebsocketService.shared.send(request: [request]) { response in
      if let assetinfo = response[0] as? [AssetInfo] {
        for info in assetinfo {
          self.store.dispatch(AssetInfoAction(assetID: info.id, info: info))
        }
      }
    }
  }
  
}

extension AppCoordinator {
  func request24hMarkets(_ pairs:[Pair], sub:Bool = true) {
    let now = Date()
    let curTime = now.timeIntervalSince1970

    var start = now.addingTimeInterval(-3600*24)
    
    let timePassed = (-start.minute * 60 - start.second).toDouble
    start = start.addingTimeInterval(timePassed)
    

    for pair in pairs {
      if let refreshTimes = app_data.pairsRefreshTimes, let oldTime = refreshTimes[pair] {
        if curTime - oldTime < 5 {
          continue
        }
      
      }
      
      UIApplication.shared.coordinator().fetchData(AssetPairQueryParams(firstAssetId: pair.base, secondAssetId: pair.quote, timeGap: 60 * 60, startTime: start, endTime: now), sub: sub)
    }
    
  }
  
  func requestKlineDetailData(pair:Pair, gap:candlesticks, vc:BaseViewController? = nil, selector:Selector?) {
    let now = Date()
    let start = now.addingTimeInterval(-gap.rawValue * 199)
    
    UIApplication.shared.coordinator().fetchKline(AssetPairQueryParams(firstAssetId: pair.base, secondAssetId: pair.quote, timeGap: gap.rawValue.toInt, startTime: start, endTime: now), gap:gap, vc:vc, selector: selector)
  }
  
  func getLatestData() {
    if AssetConfiguration.shared.asset_ids.isEmpty {
      let pairs = try! SimpleHTTPService.await(SimpleHTTPService.requestMarketList())
      
      AssetConfiguration.shared.asset_ids = pairs
      self.fetchAsset()
      self.request24hMarkets(pairs)
    }
    else {
      if app_data.assetInfo.count != AssetConfiguration.shared.asset_ids.count {
        fetchAsset()
      }
      request24hMarkets(AssetConfiguration.shared.asset_ids)
    }
   
  }
}



