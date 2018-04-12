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
  private lazy var dataSubscriber: BlockSubscriber<(OrderBook?)> = BlockSubscriber {[weak self] s in
    guard let `self` = self else { return }
        
    self.tableView.reloadData()
    self.tableView.layoutIfNeeded()
    
    DispatchQueue.main.async {
      self.coordinator?.updateMarketListHeight(500)
      self.tableView.isHidden = false
    }
  }

    @IBOutlet weak var tableView: UITableView!
    var coordinator: (OrderBookCoordinatorProtocol & OrderBookStateManagerProtocol)?

  var data:OrderBook? {
    return coordinator?.state.property.data
  }

  var pair:[String] = [] {
    didSet {
      if self.tableView != nil, oldValue != pair {
        self.tableView.isHidden = true
      }
      self.coordinator?.fetchData(pair)
    }
  }

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
  
}

extension OrderBookViewController: UITableViewDelegate, UITableViewDataSource {
  func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return data != nil ? max(data!.asks.count, data!.bids.count) : 0
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: OrderBookCell.self), for: indexPath) as! OrderBookCell

    cell.setup((data!.bids[optional:indexPath.row], data!.asks[optional:indexPath.row]), indexPath: indexPath)

    return cell
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
    
  }
}
