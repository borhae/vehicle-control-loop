package de.joachim.haensel.phd.scenario.vehicle.navigation;

public class LinearChangeParametersource 
{

	private double _segmentSize;
    private double _maxVelocity;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private double _maxLateralAcceleration;
    
    private double _segmentSizeBase;
    private double _maxVelocityBase;
    private double _maxLateralAccelerationBase;
    private double _maxLongitudinalAccelerationBase;
    private double _maxLongitudinalDeccelerationBase;

    private int _amountOfRoutes;
    private int _currentRoute;

    private double _c;
    private double _d;

    public LinearChangeParametersource(int amountOfRoutes, double from, double to, double segmentSize, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration) 
	{
		_segmentSizeBase = segmentSize;
		_maxVelocityBase = maxVelocity;
		_maxLateralAccelerationBase = maxLateralAcceleration;
		_maxLongitudinalAccelerationBase = maxLongitudinalAcceleration;
		_maxLongitudinalDeccelerationBase = maxLongitudinalDecceleration;
		
		
		_segmentSize = _segmentSizeBase;
		_maxVelocity = _maxVelocityBase;
		_maxLateralAcceleration = _maxLateralAccelerationBase;
		_maxLongitudinalAcceleration = _maxLateralAccelerationBase;
		_maxLongitudinalDecceleration = _maxLongitudinalDeccelerationBase;

		_amountOfRoutes = amountOfRoutes;
		_currentRoute = 1;
		_c = from;
		_d = to;
	}

    public double getSegmentSize()
    {
        return _segmentSize;
    }

    public double getMaxVelocity()
    {
        return _maxVelocity;
    }

    public double getMaxLongitudinalAcceleration()
    {
        return _maxLongitudinalAcceleration;
    }

    public double getMaxLongitudinalDecceleration()
    {
        return _maxLongitudinalDecceleration;
    }

    public double getMaxLateralAcceleration()
    {
        return _maxLateralAcceleration;
    }

    public void newRoute()
    {
        //if we exceed this for some reason, we just don't change any more
        if(_currentRoute < _amountOfRoutes)
        {
            // x=[a, b] to y=[c, d] -> y = (x - a) * ((d-c)/(b-a)) + c
            // a = 0.0, b = _amountOfRoutes, c = 0.6, d = 1.2, factor = y
            double factor = (_currentRoute) * ((_d - _c)/(double)_amountOfRoutes) + _c;
            _currentRoute++;
            _maxLateralAcceleration = _maxLateralAccelerationBase * factor;
            _maxLongitudinalAcceleration = _maxLateralAccelerationBase * factor;
            _maxLongitudinalDecceleration = _maxLongitudinalDeccelerationBase * factor;
        }
    }
}
