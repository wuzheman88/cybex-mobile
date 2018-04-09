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
  var data = state.data ?? Array(repeating: [], count: Config.asset_ids.count)
  var ids = state.subscribeIds ?? Array(repeating: 0, count: Config.asset_ids.count)
  var klineDatas = state.detailData ?? Array(repeating: [:], count: Config.asset_ids.count)
  
  switch action {
  case let action as AssetsFetched:
    data[action.index] = action.assets
    state.data = data
  case _ as FetchOver:
    state.haveData = true
  case let action as SubscribeSuccess:
    ids[action.index] = action.id
    state.subscribeIds = ids
  case let action as AssetInfoAction:
    state.assetInfo[action.assetID] = action.info
  case let action as kLineFetched:
    var klinedata = klineDatas[action.index]
    klinedata[action.stick] = action.assets
    klineDatas[action.index] = klinedata
    state.detailData = klineDatas
  default:
    break
  }
  
  return state
}
