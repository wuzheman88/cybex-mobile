//
//  AccountActions.swift
//  cybexMobile
//
//  Created koofrank on 2018/3/22.
//  Copyright © 2018年 Cybex. All rights reserved.
//

import Foundation
import ReSwift
import Moya

//MARK: - State
struct AccountState: StateType {
    var isLoading = false
    var page: Int = 1
    var errorMessage:String?
    var property: AccountPropertyState
}

struct AccountPropertyState {
  
}

//MARK: - Action Creator
class AccountPropertyActionCreate: LoadingActionCreator {
    public typealias ActionCreator = (_ state: AccountState, _ store: Store<AccountState>) -> Action?
    
    public typealias AsyncActionCreator = (
        _ state: AccountState,
        _ store: Store <AccountState>,
        _ actionCreatorCallback: @escaping ((ActionCreator) -> Void)
        ) -> Void
}
