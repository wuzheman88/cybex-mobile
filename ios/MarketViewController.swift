//
//  MarketViewController.swift
//  cybexMobile
//
//  Created koofrank on 2018/3/13.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import UIKit
import ReSwift
import BigInt
import DNSPageView
import SwiftTheme
import Localize_Swift

class MarketViewController: BaseViewController {
    @IBOutlet weak var pageTitleView: DNSPageTitleView!
    @IBOutlet weak var pageContentView: DNSPageContentView!
  
    @IBOutlet weak var pageContentViewHeight: NSLayoutConstraint!
    @IBOutlet weak var scrollView: UIScrollView!
    
    private lazy var contentsSubscriber: BlockSubscriber<([[Bucket]]?,[String:AssetInfo])> = BlockSubscriber {[weak self] s in
    guard let `self` = self else { return }
    
    self.refreshView()
  }
  
  

  @IBOutlet weak var pairListView: PairListHorizantalView!
  
  @IBOutlet weak var detailView: PairDetailView!
  @IBOutlet weak var kLineView: CBKLineView!
  
  var timeGap:candlesticks = .five_minute
  var indicator:indicator = .ma {
    didSet {
      switch indicator {
      case .ma:
        CBConfiguration.sharedConfiguration.main.indicatorType = .MA([7, 14, 21])

      case .ema:
        CBConfiguration.sharedConfiguration.main.indicatorType = .EMA([7, 14])

      case .macd:
        CBConfiguration.sharedConfiguration.main.indicatorType = .MACD
        self.pageContentViewHeight.constant = 300
      case .boll:
        CBConfiguration.sharedConfiguration.main.indicatorType = .BOLL(7)
      case .none:
        break
      }
    
    }
  }
  
  var curIndex:Int = 0
  var coordinator: (MarketCoordinatorProtocol & MarketStateManagerProtocol)?

	override func viewDidLoad() {
    super.viewDidLoad()
    self.localized_text = R.string.localizable.market.key.localizedContainer()
    self.view.theme_backgroundColor = [#colorLiteral(red: 0.06666666667, green: 0.0862745098, blue: 0.1294117647, alpha: 1).hexString(true), #colorLiteral(red: 0.937254902, green: 0.9450980392, blue: 0.9568627451, alpha: 1).hexString(true)]
    automaticallyAdjustsScrollViewInsets = false

    
    configLeftNavButton(nil)
    setupPageView()
  }
  
  func setupPageView() {
    let style = DNSPageStyle()
    style.titleViewBackgroundColor = UIColor.clear
    style.isShowCoverView = false
    style.bottomLineColor = #colorLiteral(red: 1, green: 0.6386402845, blue: 0.3285836577, alpha: 1)
    style.bottomLineHeight = 2
    style.isShowBottomLine = true
    style.titleColor = #colorLiteral(red: 0.5436816812, green: 0.5804407597, blue: 0.6680644155, alpha: 1)
    style.titleSelectedColor = ThemeManager.currentThemeIndex == 0 ? UIColor.white : #colorLiteral(red: 0.1399003565, green: 0.1798574626, blue: 0.2467218637, alpha: 1)
    style.titleFontSize = 14
    

    // 设置标题内容
    let titles = Localize.currentLanguage() == "en" ? ["Order Book", "Trade History"] : ["买卖单", "交易历史"]
    
    // 设置默认的起始位置
    let startIndex = 0
    
    // 对titleView进行设置
    pageTitleView.titles = titles
    pageTitleView.style = style
    pageTitleView.currentIndex = startIndex
    
    // 最后要调用setupUI方法
    pageTitleView.setupUI()
    
    
    // 创建每一页对应的controller
  
    let pair = [AssetConfiguration.CYB, AssetConfiguration.shared.asset_ids[self.curIndex]]
    let childViewControllers: [BaseViewController] = coordinator!.setupChildViewControllers(pair)
    
    self.coordinator?.refreshChildViewController(childViewControllers, pair: pair)
    
    // 对contentView进行设置
    pageContentView.childViewControllers = childViewControllers
    pageContentView.startIndex = startIndex
    pageContentView.style = style
    
    // 最后要调用setupUI方法
    pageContentView.setupUI()
    pageContentView.collectionView.panGestureRecognizer.require(toFail: UIApplication.shared.coordinator().curDisplayingCoordinator().rootVC.interactivePopGestureRecognizer!)
    
    // 让titleView和contentView进行联系起来
    pageTitleView.delegate = pageContentView
    pageContentView.delegate = pageTitleView
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
   
  }
  
  func refreshDetailView() {
    if let assets = UIApplication.shared.coordinator().state.property.sortedData {
      let data = assets[self.curIndex]
      detailView.data = data
      
      pairListView.data = [self.curIndex, assets]
      
    }

  }
  
  func refreshView() {
    self.refreshDetailView()
    fetchKlineData()
    
    let pair = [AssetConfiguration.CYB, AssetConfiguration.shared.asset_ids[self.curIndex]]
    self.coordinator?.refreshChildViewController(pageContentView.childViewControllers as! [BaseViewController], pair: pair)
  }
  
  func fetchKlineData() {
    self.startLoading()

    self.kLineView.isHidden = true
    
    UIApplication.shared.coordinator().requestKlineDetailData(sepcialID: AssetConfiguration.shared.asset_ids[self.curIndex], gap: timeGap, vc: self, selector: #selector(refreshKLine))
  }
  
  @objc func refreshKLine() {
    let oid = AssetConfiguration.shared.asset_ids[self.curIndex]
    if let klineDatas = UIApplication.shared.coordinator().state.property.detailData, klineDatas[oid]!.count > 0 {
      let klineData = klineDatas[oid]!
      guard let response = klineData[timeGap] else {
        fetchKlineData()
        return
      }
      
      endLoading()
      self.kLineView.isHidden = false

      var dataArray = [CBKLineModel]()
      for (_, data) in response.enumerated() {
        let base_assetid = data.base
        let base_info = UIApplication.shared.coordinator().state.property.assetInfo[base_assetid]!
        let base_precision = pow(10, base_info.precision.toDouble)
        let quote_assetid = data.quote
        let quote_info = UIApplication.shared.coordinator().state.property.assetInfo[quote_assetid]!
        let quote_precision = pow(10, quote_info.precision.toDouble)

        let open_price = (data.open_base.toDouble()! / base_precision)  / (data.open_quote.toDouble()! / quote_precision)
        let close_price = (data.close_base.toDouble()! / base_precision)  / (data.close_quote.toDouble()! / quote_precision)
        let high_price = (data.high_base.toDouble()! / base_precision)  / (data.high_quote.toDouble()! / quote_precision)
        let low_price = (data.low_base.toDouble()! / base_precision)  / (data.low_quote.toDouble()! / quote_precision)

        let model = CBKLineModel(date: data.open, open: open_price, close: close_price, high: high_price, low: low_price, volume: data.base_volume.toDouble()! / base_precision)
        
        
        let last_idx = dataArray.count - 1
        if last_idx >= 0 {

          let gapCount = (model.date - dataArray[last_idx].date) / timeGap.rawValue
          if gapCount > 1 {
            for _ in 1..<Int(gapCount) {
              let last_model = dataArray.last!
              let gap_model = CBKLineModel(date: last_model.date + timeGap.rawValue, open: last_model.close, close: last_model.close, high: last_model.close, low: last_model.close, volume: 0)
              dataArray.append(gap_model)
            }
          }
        }
        

        if let last_model = dataArray.last, (model.date - last_model.date) != 3600 {
          print(model.date - last_model.date)
          print("\r\n")
        }
        
        dataArray.append(model)
        
      }
      
      if dataArray.count > 0 {
        var last_model = dataArray.last!

        let surplus_count = (Date().timeIntervalSince1970 - last_model.date) / timeGap.rawValue
        if surplus_count >= 1 {
          for _ in 0..<Int(surplus_count) {
            last_model = dataArray.last!
            let gap_model = CBKLineModel(date: last_model.date + timeGap.rawValue, open: last_model.close, close: last_model.close, high: last_model.close, low: last_model.close, volume: 0)
            dataArray.append(gap_model)
          }
        }
      }
      self.kLineView.drawKLineView(klineModels: dataArray)
    }
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
      return sub.select { state in (state.property.sortedData, state.property.assetInfo) }.skipRepeats({ (old, new) -> Bool in
        if new.0 == nil || new.1.count == 0 || (old.0 == new.0 && old.1 == new.1) {
          return true
        }
        return false
      })
    }
    
    
  }
  
  deinit {
    print("dealloc")
  }
}

extension MarketViewController {
  @objc func cellClicked(_ data:[String: Any]) {
    if let index = data["index"] as? Int {
      self.curIndex = index
      
      refreshView()
    }
  }
  
  @objc func timeClicked(_ data:[String: Any]) {
    if let candlestick = data["candlestick"] as? candlesticks {
      self.timeGap = candlestick
      fetchKlineData()
    }
  }
  
  @objc func indicatorClicked(_ data:[String: Any]) {
    if let indicator = data["indicator"] as? indicator {
      self.indicator = indicator
      self.kLineView.indicator = indicator
      
      fetchKlineData()
    }
  }
}

