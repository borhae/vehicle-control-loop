package de.joachim.haensel.phd.scenario.vehicle;


public interface IVehicleFactory
{
    public void configure(IVehicleConfiguration vehicleConf);
    public IVehicle createVehicleInstance();
}
