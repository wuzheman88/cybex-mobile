let layer = UIView(frame: CGRect(x: 13, y: 70, width: 130, height: 50))
layer.layer.cornerRadius = 4
layer.layer.shadowOffset = CGSize(width: 0, height: 4)
layer.layer.shadowColor = UIColor(red:1, green:0.52, blue:0.18, alpha:1).cgColor
layer.layer.shadowOpacity = 1
layer.layer.shadowRadius = 16

let gradient = CAGradientLayer()
gradient.frame = CGRect(x: 0, y: 0, width: 130, height: 50)
gradient.colors = [
    UIColor(red:1, green:0.77, blue:0.47, alpha:0.8).cgColor,
    UIColor(red:1, green:0.57, blue:0.26, alpha:0.8).cgColor
]
gradient.locations = [0, 1]
gradient.startPoint = CGPoint.zero
gradient.endPoint = CGPoint(x: 1, y: 0.89)
gradient.cornerRadius = 4
layer.layer.addSublayer(gradient)

self.view.addSubview(layer)