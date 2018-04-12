//
//  Collection+Extensions.swift
//  cybexMobile
//
//  Created by koofrank on 2018/4/12.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation

extension Collection {
  subscript(optional i: Index) -> Iterator.Element? {
    return self.indices.contains(i) ? self[i] : nil
  }
}
