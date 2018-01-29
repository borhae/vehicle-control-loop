package de.joachim.haensel.vrepshapecreation.joints;

import java.util.List;

import de.joachim.haensel.vrepshapecreation.parameters.Parameters;

public class JointParameters extends Parameters
{
    float _length;
    float _diameter;
    boolean _isCyclic;
    EVRepJointTypes _type;
    EVRepJointModes _mode;
    float[] _interval;
    private boolean _isMotorEnabled;
    private float _targetVelocity;
    private float _maximumForce;
    private boolean _isCtrlLoopEnabled;
    private float _targetPosition;
    private boolean _inSpringDamperMode;
    private float _springConstantK;
    private float _dampingCoefficientC;

    public JointParameters()
    {
        _x = 0.0f;
        _y = 0.0f;
        _z = 0.0f;
        _length = 0.01f;
        _diameter = 0.01f;
        _alpha = 0.0f;
        _beta = 0.0f;
        _gamma = 0.0f;
        _isCyclic = false;
        _isMotorEnabled = false;
        _targetVelocity = 0.0f;
        _maximumForce = 0.0f;
        _isCtrlLoopEnabled = false;
        _targetPosition = 0.0f;
        _inSpringDamperMode = false;
        _springConstantK = 0.0f;
        _dampingCoefficientC = 0.0f;
        _type = EVRepJointTypes.REVOLUTE;
        _mode = EVRepJointModes.FORCE;
        _interval = new float[2];
        _interval[0] = 0.0f;
        _interval[1] = 0.0f;
    }
    
    public void setSize(float length, float diameter)
    {
        _length = length;
        _diameter = diameter;
    }

    public void setCyclic(boolean isCyclic)
    {
        _isCyclic = isCyclic;
    }
    
    public void setMotorEnabled(boolean isMotorEnabled)
    {
        _isMotorEnabled = isMotorEnabled;
    }

    public boolean isMotorEnabled()
    {
        return _isMotorEnabled;
    }
    
    public void setControlLoopEnabled(boolean isCtrlLoopEnabled)
    {
        _isCtrlLoopEnabled = isCtrlLoopEnabled;
    }

    public boolean isCtrlLoopEnabled()
    {
        return _isCtrlLoopEnabled;
    }
    

    public void setSpringDamperMode(boolean inSpringDamperMode)
    {
        _inSpringDamperMode = inSpringDamperMode;
    }

    public boolean isInSpringDamperMode()
    {
        return _inSpringDamperMode;
    }
    
    public void setType(EVRepJointTypes type)
    {
        _type = type;
    }

    public void setMode(EVRepJointModes mode)
    {
        _mode = mode;
    }
    
    public void setInterval(float[] interval)
    {
        _interval = interval;
    }

    public void setSpringConstantK(float k)
    {
        _springConstantK = k;
    }
    
    public float getSpringConstantK()
    {
        return _springConstantK;
    }

    public float getDampingCoefficientC()
    {
        return _dampingCoefficientC;
    }

    public void setTargetVelocity(float targetVelocity)
    {
        _targetVelocity = targetVelocity;
    }

    public void setMaximumForce(float maximumForce)
    {
        _maximumForce = maximumForce;
    }

    public void setTargetPosition(float targetPosition)
    {
        _targetPosition = targetPosition;
    }

    public void setDampingCoefficientC(float dampingCoefficientC)
    {
        _dampingCoefficientC = dampingCoefficientC;
    }

    @Override
    protected void populateInts(List<Integer> paramList)
    {
        paramList.add(_type.getValue());
        paramList.add(_mode.getValue());
        paramList.add(_isCyclic ? 1 : 0);
    }

    @Override
    protected void populateFloats(List<Float> paramList)
    {
        paramList.add(_length);
        paramList.add(_diameter);
        paramList.add(_interval[0]);
        paramList.add(_interval[1]);
        paramList.add(_targetVelocity);
        paramList.add(_maximumForce);
        paramList.add(_targetPosition);
    }
}
