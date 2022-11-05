
package org.crawl.http.vo;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:43:20
 */
public class Vehicle {

    private int id;
    private String platenumber;
    private String color;

    public Vehicle() {
    }

    public int getId() {
        return id;
    }

    public Vehicle(int id, String platenumber, String color) {
        this.id = id;
        this.platenumber = platenumber;
        this.color = color;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlatenumber() {
        return platenumber;
    }

    public void setPlatenumber(String platenumber) {
        this.platenumber = platenumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Vehicle [id=" + id + ", platenumber=" + platenumber + ", color=" + color + "]";
    }
}
