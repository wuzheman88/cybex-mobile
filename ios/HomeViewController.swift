//
//  HomeViewController.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift
import EZSwiftExtensions

class HomeViewController: BaseViewController, UINavigationControllerDelegate, UIScrollViewDelegate {
  var coordinator: (HomeCoordinatorProtocol & HomeStateManagerProtocol)?
  
  private lazy var contentsSubscriber: BlockSubscriber<([[Asset]]?,[assetID:AssetInfo])> = BlockSubscriber {[weak self] s in
    guard let `self` = self else { return }
    
    self.tableView.reloadData()
  }
  
  private lazy var loadOverSubscriber: BlockSubscriber<Bool> = BlockSubscriber {[weak self] s in
    guard let `self` = self else { return }
    
    if (s) {
      self.endLoading()
    }
  }

  
  @IBOutlet weak var tableView: UITableView!
  
  override func viewDidLoad() {
    super.viewDidLoad()

    setupUI()
    requestData()
  }
  
  
  func setupUI() {    
    if #available(iOS 11.0, *) {
      navigationItem.largeTitleDisplayMode = .always
    }
    
    
    self.localized_text = R.string.localizable.navWatchlist.key.localizedContainer()
    
    let cell = String.init(describing: HomePairCell.self)
    tableView.register(UINib.init(nibName: cell, bundle: nil), forCellReuseIdentifier: cell)
    
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
  }

  func requestData() {
    self.startLoading()
    UIApplication.shared.coordinator().request24hMarkets()
    UIApplication.shared.coordinator().fetchAsset(assetID.all)
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
    
    UIApplication.shared.coordinator().subscribe(contentsSubscriber) { sub in
      return sub.select { state in (state.property.data, state.property.assetInfo) }.skipRepeats({ (old, new) -> Bool in
        if new.0 == nil || new.1.count == 0 {
          return true
        }
        return false
      })
    }
    
    UIApplication.shared.coordinator().subscribe(loadOverSubscriber) { sub in
      return sub.select { state in state.property.haveData }.skipRepeats({ (old, new) -> Bool in
        return false
      })
    }
   
  }
  
}

extension HomeViewController: UITableViewDelegate, UITableViewDataSource {
  func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    let data = UIApplication.shared.coordinator().state.property.data ?? []
    return data.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: HomePairCell.self), for: indexPath) as! HomePairCell
    if let assets = UIApplication.shared.coordinator().state.property.data {
      let data = assets[indexPath.row]
      cell.setup(data, indexPath: indexPath)
    }
    return cell
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {

  
  }
}

extension HomeViewController {
  @objc func cellClicked(_ data:[String: Any]) {
    if let index = data["index"] as? Int {
      self.coordinator?.openMarket(index:index)

    }
  }

}




