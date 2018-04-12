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
    
    state.data = limitOrders_to_OrderBook(orders: action.data, base: action.base)
    
    default:
        break
    }
    
    return state
}

func limitOrders_to_OrderBook(orders: [LimitOrder], base:assetID) -> OrderBook {
  var bids:[OrderBook.Order] = []
  var asks:[OrderBook.Order] = []
  
  var bids_total_amount:[Double] = []
  var asks_total_amount:[Double] = []

  for order in orders {
    let sellPrice_base = order.sellPrice.base
    if sellPrice_base.assetID == base {
      bids_total_amount.append(order.forSale.toDouble()!)
    }
    else {
      asks_total_amount.append(order.forSale.toDouble()!)
    }
  }
  
  for order in orders {
    let sellPrice_base = order.sellPrice.base
    if sellPrice_base.assetID == base {
      let percent = bids_total_amount[0...bids.count].reduce(0, +) / bids_total_amount.reduce(0, +)
      
      let precision_ratio = pow(10, order.sellPrice.base.info().precision.toDouble) / pow(10, order.sellPrice.quote.info().precision.toDouble)

      let quote_forSale = order.forSale.toDouble()! / (precision_ratio * order.sellPrice.toReal())
      let quote_volume = quote_forSale / pow(10, order.sellPrice.quote.info().precision.toDouble)
      
      let bid = OrderBook.Order(price: order.sellPrice.toReal().toString.formatCurrency(digitNum: order.sellPrice.base.info().precision), volume: quote_volume.toString.suffixNumber(digitNum: 10 - order.sellPrice.base.info().precision), volume_percent: percent)
      bids.append(bid)
    }
    else {
      let percent = asks_total_amount[0...asks.count].reduce(0, +) / asks_total_amount.reduce(0, +)
      let quote_volume = order.forSale.toDouble()! / pow(10, sellPrice_base.info().precision.toDouble)
      
      let ask = OrderBook.Order(price: (1.0 / order.sellPrice.toReal()).toString.formatCurrency(digitNum: order.sellPrice.quote.info().precision), volume: quote_volume.toString.suffixNumber(digitNum: 10 - order.sellPrice.quote.info().precision), volume_percent: percent)
      asks.append(ask)
    }
  }
  
  return OrderBook(bids: bids, asks: asks)
  
}



