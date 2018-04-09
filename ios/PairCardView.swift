//
//  PairCardView.swift
//  cybexMobile
//
//  Created by koofrank on 2018/3/13.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import EZSwiftExtensions
import SwiftTheme

@IBDesignable
class PairCardView : UIView {
  enum event:String {
    case cellClicked
  }

    @IBOutlet weak var base_name: UILabel!
    @IBOutlet weak var quote_name: UILabel!
    @IBOutlet weak var arrowIcon: UIImageView!
    @IBOutlet weak var change: UILabel!
  
  @IBOutlet weak var timeLabel: UILabel!
  
  lazy var gradient = self.gradientLayer()

    var isSelected:Bool = false {
    didSet {
      if isSelected {
        gradient.removeFromSuperlayer()
        gradient = self.gradientLayer()

        self.subviews[0].layer.insertSublayer(gradient, at: 0)
        self.arrowIcon.image = #imageLiteral(resourceName: "ic_arrow_drop_down_white_24px")
        self.quote_name.textColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 0.5)
        self.change.textColor = #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
        self.timeLabel.textColor = #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
        self.base_name.textColor = #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)

      }
      else {
        gradient.removeFromSuperlayer()
        
        self.arrowIcon.image = #imageLiteral(resourceName: "ic_arrow_drop_down_24px")
        self.quote_name.textColor = #colorLiteral(red: 0.5436816812, green: 0.5804407597, blue: 0.6680644155, alpha: 1)
        self.change.textColor = #colorLiteral(red: 0.5436816812, green: 0.5804407597, blue: 0.6680644155, alpha: 1)
        self.timeLabel.textColor = #colorLiteral(red: 0.5436816812, green: 0.5804407597, blue: 0.6680644155, alpha: 1)
        self.base_name.textColor = self.base_name.themeTitleColor
      }
    }
      
  }
  
  var data: Any? {
    didSet {
      guard let markets = data as? [Asset], markets.count > 0 , UIApplication.shared.coordinator().state.property.assetInfo.count > 0 else {
        let index = self.store["index"] as! Int
        
        let pair = Config.asset_ids[index]
        let base_asset = assetID(rawValue: pair[0])!
        let quote_asset = assetID(rawValue: pair[1])!
        let base_info = UIApplication.shared.coordinator().state.property.assetInfo[base_asset]
        let quote_info = UIApplication.shared.coordinator().state.property.assetInfo[quote_asset]
        
        self.base_name.text = base_info != nil ? base_info!.symbol : "-"
        self.quote_name.text = quote_info != nil ? ("/" + quote_info!.symbol) : "/-"
        self.change.text = "-"
        self.arrowIcon.transform = CGAffineTransform(rotationAngle: CGFloat.pi / 2.0)

        return
      }
      
      let matrix = AssetMatrix(markets)
      self.base_name.text = matrix.base_name
      self.quote_name.text = "/" + matrix.quote_name
      self.change.text = (matrix.incre == .greater ? "+" : "") + matrix.change + "%"
      
      switch matrix.incre {
      case .greater:
        self.arrowIcon.transform = CGAffineTransform.identity
      case .less:
        self.arrowIcon.transform = CGAffineTransform(rotationAngle: CGFloat.pi)

      case .equal:
        self.arrowIcon.transform = CGAffineTransform(rotationAngle: CGFloat.pi / 2.0)

      }
    }
  }
  
  
  fileprivate func setup() {
    self.arrowIcon.transform = CGAffineTransform(rotationAngle: CGFloat.pi / 2.0)

    self.isUserInteractionEnabled = true

    self.rx.tapGesture().when(.recognized).subscribe(onNext: {[weak self] tap in
      guard let `self` = self else { return }
      
      self.next?.sendEventWith(event.cellClicked.rawValue, userinfo: ["index": self.store["index"] ?? []])
      
    }).disposed(by: disposeBag)

  }
  
  func gradientLayer() -> CAGradientLayer {
    let layer = CAGradientLayer()
    layer.frame = CGRect(origin: CGPoint.zero, size: self.size)
    layer.colors = [UIColor(red:1, green:0.77, blue:0.47, alpha:0.8).cgColor,  UIColor(red:1, green:0.57, blue:0.26, alpha:0.8).cgColor]
    layer.locations = [0, 1]
    layer.startPoint = CGPoint.zero
    layer.endPoint = CGPoint(x: 1, y: 0.89)
    return layer
  }
  
  override var intrinsicContentSize: CGSize {
    return CGSize.init(width: UIViewNoIntrinsicMetric,height: dynamicHeight())
  }
  
  fileprivate func updateHeight() {
    layoutIfNeeded()
    self.height = dynamicHeight()
    invalidateIntrinsicContentSize()
  }
  
  fileprivate func dynamicHeight() -> CGFloat {
    let lastView = self.subviews.last?.subviews.last
    return lastView!.bottom
  }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    layoutIfNeeded()
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    loadViewFromNib()
    setup()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    loadViewFromNib()
    setup()
  }
  
  fileprivate func loadViewFromNib() {
    let bundle = Bundle(for: type(of: self))
    let nibName = String(describing: type(of: self))
    let nib = UINib.init(nibName: nibName, bundle: bundle)
    let view = nib.instantiate(withOwner: self, options: nil).first as! UIView
    
    addSubview(view)
    view.frame = self.bounds
    view.autoresizingMask = [.flexibleHeight, .flexibleWidth]
  }

}
