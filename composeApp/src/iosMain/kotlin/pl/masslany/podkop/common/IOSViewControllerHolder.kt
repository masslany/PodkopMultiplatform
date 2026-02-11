package pl.masslany.podkop.common

import platform.UIKit.UIViewController

class IOSViewControllerHolder {
    var provider: () -> UIViewController? = { null }
}
