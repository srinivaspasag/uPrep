import UIKit
import WebKit

// Hosts the deployed UPrep web app in a full-screen WKWebView. File uploads
// (<input type="file">) and downloads are handled natively by WKWebView on iOS.
final class WebViewController: UIViewController, WKNavigationDelegate, WKUIDelegate {

    // The deployed UPrep web app. Change this to your domain/IP as needed.
    private let baseURL = URL(string: "https://65.2.108.70.sslip.io")!

    private var webView: WKWebView!
    private let refreshControl = UIRefreshControl()

    override func loadView() {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        config.websiteDataStore = .default()

        webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.allowsBackForwardNavigationGestures = true
        webView.scrollView.contentInsetAdjustmentBehavior = .never
        view = webView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        refreshControl.addTarget(self, action: #selector(reloadPage), for: .valueChanged)
        webView.scrollView.refreshControl = refreshControl
        webView.load(URLRequest(url: baseURL))
    }

    @objc private func reloadPage() {
        webView.reload()
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        refreshControl.endRefreshing()
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        refreshControl.endRefreshing()
    }

    // Keep UPrep navigation inside the app; hand external links to Safari.
    func webView(
        _ webView: WKWebView,
        decidePolicyFor navigationAction: WKNavigationAction,
        decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
    ) {
        if navigationAction.navigationType == .linkActivated,
           let url = navigationAction.request.url,
           let host = url.host,
           !host.contains("65.2.108.70.sslip.io") {
            UIApplication.shared.open(url)
            decisionHandler(.cancel)
            return
        }
        decisionHandler(.allow)
    }

    // Load target="_blank" / window.open links in the same web view.
    func webView(
        _ webView: WKWebView,
        createWebViewWith configuration: WKWebViewConfiguration,
        for navigationAction: WKNavigationAction,
        windowFeatures: WKWindowFeatures
    ) -> WKWebView? {
        if navigationAction.targetFrame == nil, let url = navigationAction.request.url {
            webView.load(URLRequest(url: url))
        }
        return nil
    }

    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }
}
