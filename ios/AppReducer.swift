//
//  AppReducer.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift

public class BlockSubscriber<S>: StoreSubscriber {
  public typealias StoreSubscriberStateType = S
  private let block: (S) -> Void
  
  public init(block: @escaping (S) -> Void) {
    self.block = block
  }
  
  public func newState(state: S) {
    self.block(state)
  }
}

let TrackingMiddleware: Middleware<Any> = { dispatch, getState in
  return { next in
    return { action in
      
      if let action = action as? StartLoading {
       
        action.vc?.startLoading()
      }
      else if let action = action as? EndLoading {
        action.vc?.endLoading()
        
      }
      else if let action = action as? RefreshState {
        action.vc?.perform(action.sel)
      }
      
      return next(action)
    }
  }
}


func loadingReducer(_ state: Bool?, action: Action) -> Bool {
  var state = state ?? false
  
  switch action {
  case _ as StartLoading:
    state = true
  case _ as EndLoading:
    state = false
  default:
    break
  }
  
  return state
}

func errorMessageReducer(_ state: String?, action: Action) -> String {
  var state = state ?? ""
  
  switch action {
  case let action as NetworkErrorMessage:
    state = action.errorMessage
  case _ as CleanErrorMessage:
    state = ""
  default:
    break
  }
  
  return state
}

func pageReducer(_ state: Int?, action: Action) -> Int {
  var state = state ?? 1
  
  switch action {
  case _ as NextPage:
    state = state + 1
  case _ as ResetPage:
    state = 1
  default:
    break
  }
  
  return state
}


func AppReducer(action:Action, state:AppState?) -> AppState {
  return AppState(property: AppPropertyReducer(state?.property, action: action))
}

func AppPropertyReducer(_ state: AppPropertyState?, action: Action) -> AppPropertyState {
  var state = state ?? AppPropertyState()
  var data = state.data ?? [:]

  var ids = state.subscribeIds ?? [:]
  var klineDatas = state.detailData ?? [:]
  
  switch action {
  case let action as AssetsFetched:
    if action.assets.count != 0  {
      data[action.pair.secondAssetId] = action.assets
    }
    else if action.assets.count == 0 {
      data[action.pair.secondAssetId] = []
    }
    
    var sortingData = Array(data.values)
    
    if data.count > 1 {
      sortingData.sort { (last, cur) -> Bool in
        if last.count == 0 && cur.count != 0 {
          return false
        }
        else if last.count != 0 && cur.count == 0 {
          return true
        }
        else if last.count == 0 && cur.count == 0 {
          return false
        }
        
        return BucketMatrix.init(last).base_volume_origin > BucketMatrix.init(cur).base_volume_origin
      }
    }
    
    state.data = data
    state.sortedData = sortingData
  case _ as FetchOver:
    state.haveData = true
  case let action as SubscribeSuccess:
    ids[action.assetID] = action.id
    state.subscribeIds = ids
  case let action as AssetInfoAction:
    state.assetInfo[action.assetID] = action.info
  case let action as kLineFetched:
    if klineDatas.has(action.assetID) {
      var klineData = klineDatas[action.assetID]!
      klineData[action.stick] = action.assets
      klineDatas[action.assetID] = klineData
    }
    else {
      klineDatas[action.assetID] = [action.stick: action.assets]
    }
    state.detailData = klineDatas
    
  default:
    break
  }
  
  return state
}
