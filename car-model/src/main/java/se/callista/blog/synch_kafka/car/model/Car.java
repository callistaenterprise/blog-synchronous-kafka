package se.callista.blog.synch_kafka.car.model;

public class Car {

  private String vin;
  private String regNo;

  public Car() {};

  public Car(String vin, String regNo) {
    super();
    this.vin = vin;
    this.regNo = regNo;
  }

  public String getVin() {
    return vin;
  }

  public void setVin(String vIN) {
    vin = vIN;
  }

  public String getRegNo() {
    return regNo;
  }

  public void setRegNo(String regNo) {
    this.regNo = regNo;
  }

  @Override
  public String toString() {
    return "Car [vin=" + vin + ", regNo=" + regNo + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((regNo == null) ? 0 : regNo.hashCode());
    result = prime * result + ((vin == null) ? 0 : vin.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Car other = (Car) obj;
    if (regNo == null) {
      if (other.regNo != null)
        return false;
    } else if (!regNo.equals(other.regNo))
      return false;
    if (vin == null) {
      if (other.vin != null)
        return false;
    } else if (!vin.equals(other.vin))
      return false;
    return true;
  }

}
