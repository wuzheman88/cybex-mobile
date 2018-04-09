//
//  AppActions.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift

struct AppState:StateType {
  var property: AppPropertyState
}
struct AppPropertyState {
  var haveData:Bool = false

  var data:[[Asset]]?
  var detailData:[[candlesticks:[Asset]]]?

  var subscribeIds:[Int]?
  
  var assetInfo:[assetID:AssetInfo] = [:]
}

class LoadingActionCreator {
  var viewController: BaseViewController?
  
  init(vc: BaseViewController?) {
    self.viewController = vc
  }
}

// MARK: - Common Actions
struct StartLoading: Action {
  var vc: BaseViewController?
}
struct EndLoading: Action {
  var vc: BaseViewController?
}

struct NoData: Action {
}

struct NetworkErrorMessage: Action {
  let errorMessage:String
}
struct CleanErrorMessage: Action {}

struct NextPage: Action {}

struct ResetPage: Action {}


struct AssetsFetched:Action {
  let index:Int
  let assets:[Asset]
}

struct kLineFetched:Action {
  let index:Int
  let stick:candlesticks
  let assets:[Asset]
}

struct FetchOver:Action {
  
}

struct FetchKlineOver:Action {
  
}

struct RefreshState:Action {
  let sel:Selector
  let vc:BaseViewController?
}

struct SubscribeSuccess:Action {
  let index:Int
  let id:Int
}

struct AssetInfoAction:Action {
  let assetID:assetID
  let info:AssetInfo
}

typealias MarketDataCallback = (Int, [Asset]) -> Void

class AppPropertyActionCreate: LoadingActionCreator {
  public typealias ActionCreator = (_ state: AppState, _ store: Store<AppState>) -> Action?
  
  public typealias AsyncActionCreator = (
    _ state: AppState,
    _ store: Store <AppState>,
    _ actionCreatorCallback: @escaping ((ActionCreator) -> Void)
    ) -> Void
  
  func fetchAssets(with sub:Bool = true, params:AssetPairQueryParams, callback:MarketDataCallback?) -> ActionCreator {
    return { state, store in
      var index = 0
      for (idx, pair) in Config.asset_ids.enumerated() {
        if pair[0] == params.firstAssetId, pair[1] == params.secondAssetId {
          index = idx
        }
      }
      
      self.fetchingMarketList(params, callback: {[weak self] (res) in
        guard let `self` = self else { return }
        
        if let response = res as? [[Asset?]], var assets = response[0] as? [Asset] {
          if assets.count > 0 {
            let asset = assets[0]
            
            if asset.open > params.startTime.timeIntervalSince1970 {
              
              self.cycleFetch(asset, params: params, callback: { (o_asset) in
                if let o_asset = o_asset as? Asset {
                  let close = o_asset.close_base
                  let quote_close = o_asset.close_quote
                  let addAsset = asset.copy() as! Asset
                  
                  let gapCount = ceil((asset.open - params.startTime.timeIntervalSince1970) / asset.seconds.toDouble()!)
                  addAsset.close_base = close
                  addAsset.close_quote = quote_close
                  addAsset.open_base = close
                  addAsset.open_quote = quote_close
                  addAsset.high_base = close
                  addAsset.high_quote = quote_close
                  addAsset.low_base = close
                  addAsset.low_quote = quote_close
                  addAsset.base_volume = "0"
                  addAsset.quote_volume = "0"
                  addAsset.open = asset.open - gapCount * asset.seconds.toDouble()!
                  assets.insertFirst(addAsset)
                  
      
                }
                
                callback?(index, assets)

              })
              
            }
            else {
              callback?(index, assets)
            }
            
          }
          else {
            callback?(index, [])
          }
        }
      })
      
 
      
      if sub {
        let subRequest = SubscribeMarketRequest(ids: [params.firstAssetId, params.secondAssetId])
     
        NetWorkService.shared.send(request: [subRequest], callback: { response in
          if let id = response[0] as? Int {
            store.dispatch(SubscribeSuccess(index:index, id: id))
          }
        })
      }
      
      return nil
      
    }
  }
  
  func fetchingMarketList(_  params:AssetPairQueryParams, callback:CommonAnyCallback?) {
    let request = GetMarketHistoryRequest(queryParams: params)
    
    NetWorkService.shared.send(request: [request]) { (response) in
      if let callback = callback {
        callback(response)
      }
    }
  }
  
  func cycleFetch(_ asset:Asset, params:AssetPairQueryParams, callback:CommonAnyCallback?) {
    var re_params = params
    re_params.startTime = Date(timeIntervalSince1970: asset.open - 86400)
    re_params.endTime = Date(timeIntervalSince1970: asset.open - 3600)
    self.fetchingMarketList(re_params, callback: {[weak self] (o_res) in
      guard let `self` = self else { return }
      if let o_response = o_res as? [[Asset?]], let o_assets = o_response[0] as? [Asset] {
        if o_assets.count > 0, let o_asset = o_assets.last {
          if let callback = callback {
            callback(o_asset)
          }
        }
        else if o_assets.count > 0 {
          self.cycleFetch(asset, params: params, callback: callback)
        }
        else {
          if let callback = callback {
            callback(0)
          }
        }
      }
    })
  }
  
}
