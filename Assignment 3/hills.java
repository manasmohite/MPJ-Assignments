/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lca_02;

/**
 *
 * @author kriii
**/
// File: Hillstations.java

class hillstations {

    void famousfor() {
        System.out.println("Hill station is famous for its scenic beauty.");
    }

    void famousfood() {
        System.out.println("Hill station has local traditional food.");
    }
}
class Manali extends hillstations {

    @Override
    void famousfor() {
        System.out.println("Manali is famous for snow and adventure sports.");
    }

    @Override
    void famousfood() {
        System.out.println("Manali is famous for Siddu and Trout fish.");
    }
}
class Ooty extends hillstations {

    @Override
    void famousfor() {
        System.out.println("Ooty is famous for tea gardens.");
    }

    @Override
    void famousfood() {
        System.out.println("Ooty is famous for homemade chocolates.");
    }
}
class Munnar extends hillstations {

    @Override
    void famousfor() {
        System.out.println("Munnar is famous for lush green hills and tea plantations.");
    }

    @Override
    void famousfood() {
        System.out.println("Munnar is famous for Kerala cuisine.");
    }
}
