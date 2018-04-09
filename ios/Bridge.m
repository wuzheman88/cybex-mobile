//
//  Bridge.m
//  cybexMobile
//
//  Created by koofrank on 2018/3/22.
//  Copyright © 2018年 Cybex. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ManagerBridger,NSObject)
RCT_EXTERN_METHOD(sendEvent:(NSString *)name payload:(NSDictionary *)payload)

RCT_EXTERN_METHOD(supportedEvents)

@end
