/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lca_02;

/**
 *
 * @author kriii
 */
public class mainhillstations {

    public static void main(String[] args) {

        //  Base class reference, subclass objects
        hillstations h;

        // Manali
        h = new Manali();
        h.famousfor();
        h.famousfood();

        System.out.println();

        // Ooty
        h = new Ooty();
        h.famousfor();
        h.famousfood();

        System.out.println();

        // Munnar
        h = new Munnar();
        h.famousfor();
        h.famousfood();
    }
    
}
