//
//  OrderBookViewController.swift
//  cybexMobile
//
//  Created koofrank on 2018/4/8.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa
import ReSwift
import EZSwiftExtensions
import SwiftyJSON

class OrderBookViewController: BaseViewController {
  private lazy var dataSubscriber: BlockSubscriber<([JSON]?)> = BlockSubscriber {[weak self] s in
    guard let `self` = self else { return }
    
    
    self.data = []
    self.buy_volumes = []
    self.sell_volumes = []
    

    self.convertToData()
    
    self.tableView.reloadData()
    self.tableView.layoutIfNeeded()
    
    DispatchQueue.main.async {
      self.coordinator?.updateMarketListHeight(500)
      self.tableView.isHidden = false
    }
  }

    @IBOutlet weak var tableView: UITableView!
    var coordinator: (OrderBookCoordinatorProtocol & OrderBookStateManagerProtocol)?


  var pair:[String] = [] {
    didSet {
      if self.tableView != nil, oldValue != pair {
        self.tableView.isHidden = true
      }
      self.coordinator?.fetchData(pair)
    }
  }
  
  var data:[[String]] = []
  var buy_volumes:[Double] = []
  var sell_volumes:[Double] = []

	override func viewDidLoad() {
    super.viewDidLoad()
    
    let cell = String.init(describing: OrderBookCell.self)
    tableView.register(UINib.init(nibName: cell, bundle: nil), forCellReuseIdentifier: cell)
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
  }

    func commonObserveState() {
        coordinator?.subscribe(errorSubscriber) { sub in
            return sub.select { state in state.errorMessage }.skipRepeats({ (old, new) -> Bool in
                return false
            })
        }
        
        coordinator?.subscribe(loadingSubscriber) { sub in
            return sub.select { state in state.isLoading }.skipRepeats({ (old, new) -> Bool in
                return false
            })
        }
    }
    
    override func configureObserveState() {
        commonObserveState()
      
      coordinator?.subscribe(dataSubscriber) { sub in
        return sub.select { state in (state.property.data) }.skipRepeats({ (old, new) -> Bool in
          return false
        })
      }
    }
  
  func convertToData() {
    if let data = self.coordinator?.state.property.data {
      var showData:[[String]] = []
      
      for d in data {
        let curData = d

        let base = pair[0]
        let quote = pair[1]
        let quote_data = curData[0]
        let base_data = curData[1]
        
        
        let base_info = UIApplication.shared.coordinator().state.property.assetInfo[assetID(rawValue: base)!]!
        let quote_info = UIApplication.shared.coordinator().state.property.assetInfo[assetID(rawValue: quote)!]!
        let base_precision = pow(10, base_info.precision.toDouble)
        let quote_precision = pow(10, quote_info.precision.toDouble)
        
        let buy_volume = quote_data["base"]["amount"].stringValue.toDouble()! / base_precision
        let buy_forsale = curData[2].stringValue.toDouble()! / base_precision
        
        let buy_price = buy_volume / (quote_data["quote"]["amount"].stringValue.toDouble()! / quote_precision)
        buy_volumes.append(buy_volume)

        let sell_volume = base_data["quote"]["amount"].stringValue.toDouble()! / base_precision
        let sell_price = sell_volume / (base_data["base"]["amount"].stringValue.toDouble()! / quote_precision)
        let sell_forsale = curData[3].stringValue.toDouble()! / base_precision

        sell_volumes.append(sell_volume)

        showData.append([buy_price.toString.formatCurrency(digitNum: base_info.precision),
                         buy_forsale.toString,
                         sell_price.toString.formatCurrency(digitNum: base_info.precision),
                         sell_forsale.toString])
        
        self.data = showData
      }
      
    }
   
  }
}

extension OrderBookViewController: UITableViewDelegate, UITableViewDataSource {
  func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return data.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: OrderBookCell.self), for: indexPath) as! OrderBookCell
   
    let buy_total = buy_volumes.reduce(0, +)
    let sell_total = sell_volumes.reduce(0, +)
    let left = buy_volumes[0..<(indexPath.row + 1)].reduce(0, +)
    let right = sell_volumes[0..<(indexPath.row + 1)].reduce(0, +)

    let left_fraction = buy_total == 0 ? 0 : (left / buy_total)
    let right_fraction = sell_total == 0 ? 0 : (right / sell_total)

    cell.setup((data[indexPath.row], left_fraction, right_fraction), indexPath: indexPath)

    return cell
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
    
  }
}
