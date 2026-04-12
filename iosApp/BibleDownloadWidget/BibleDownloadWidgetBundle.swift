//
//  BibleDownloadWidgetBundle.swift
//  BibleDownloadWidget
//
//  Created by Pierre Vieira on 11/04/26.
//

import WidgetKit
import SwiftUI

@main
struct BibleDownloadWidgetBundle: WidgetBundle {
    var body: some Widget {
        BibleDownloadWidget()
        BibleDownloadWidgetControl()
        BibleDownloadWidgetLiveActivity()
    }
}
