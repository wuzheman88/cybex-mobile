//
//  OrderBookReducers.swift
//  cybexMobile
//
//  Created koofrank on 2018/4/8.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import UIKit
import ReSwift

func OrderBookReducer(action:Action, state:OrderBookState?) -> OrderBookState {
    return OrderBookState(isLoading: loadingReducer(state?.isLoading, action: action), page: pageReducer(state?.page, action: action), errorMessage: errorMessageReducer(state?.errorMessage, action: action), property: OrderBookPropertyReducer(state?.property, action: action))
}

func OrderBookPropertyReducer(_ state: OrderBookPropertyState?, action: Action) -> OrderBookPropertyState {
  var state = state ?? OrderBookPropertyState()
  
  switch action {
  case let action as FetchedLimitData:
    state.data = action.data
    default:
        break
    }
    
    return state
}



