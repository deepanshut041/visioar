package life.plank.visior.data.view

data class ScreenData(val azimuth: Int, val pitch:Int, val roll:Int, val lat: Double, val lon: Double, val points: List<ArSelectedPoint>)