//
//  OrderBookActions.swift
//  cybexMobile
//
//  Created koofrank on 2018/4/8.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift
import SwiftyJSON

//MARK: - State
struct OrderBookState: StateType {
    var isLoading = false
    var page: Int = 1
    var errorMessage:String?
    var property: OrderBookPropertyState
}

struct OrderBookPropertyState {
  var data:OrderBook?
}

struct OrderBook {
  struct Order {
    let price:String
    let volume:String
    
    let volume_percent:Double
  }
  
  let bids:[Order]
  let asks:[Order]
}

struct FetchedLimitData:Action {
  let data:[LimitOrder]
  let base:String
}

//MARK: - Action Creator
class OrderBookPropertyActionCreate: LoadingActionCreator {
    public typealias ActionCreator = (_ state: OrderBookState, _ store: Store<OrderBookState>) -> Action?
    
    public typealias AsyncActionCreator = (
        _ state: OrderBookState,
        _ store: Store <OrderBookState>,
        _ actionCreatorCallback: @escaping ((ActionCreator) -> Void)
        ) -> Void
  
  
  func fetchLimitOrders(with ids:[String], callback:CommonAnyCallback?) -> ActionCreator {
    return { state, store in
     
      let request = getLimitOrdersRequest(ids: ids)
      
      NetWorkService.shared.send(request: [request]) { (response) in
        if let callback = callback {
          callback(response[0])
        }
      }
      
      return nil
      
    }
  }
}
