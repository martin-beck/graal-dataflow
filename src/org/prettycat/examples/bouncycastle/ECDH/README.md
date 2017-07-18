$ wget https://www.bouncycastle.org/download/bcprov-jdk15on-157.jar
$ javac -cp .:bcprov-jdk15on-157.jar ECDH_BC.java
$ java -cp .:bcprov-jdk15on-157.jar ECDH_BC
Alice: EC Private Key
             S: 7b957721204d4290edcb5f1420988ddd9ab9ffc97c8905dd

Alice: EC Public Key
            X: 4384a964ca4840b4a085054764fa2f597a16a42ea822896
            Y: 62e33641881715e624851242fd7ab360dd8a908dc35d3a7a

Bob:   EC Private Key
             S: f8ec571d10500474e577633e75342319d779f2ad345ac3f7

Bob:   EC Public Key
            X: 8dcaf55783a679ae29d0cd93a236187988e3f43636e8ec8f
            Y: 7c7613d284c0b054fea567a6790c91a91cde96d1178d9a3d

Alice Prv: 7b957721204d4290edcb5f1420988ddd9ab9ffc97c8905dd
Alice Pub: 0204384a964ca4840b4a085054764fa2f597a16a42ea822896
Bob Prv:   00f8ec571d10500474e577633e75342319d779f2ad345ac3f7
Bob Pub:   038dcaf55783a679ae29d0cd93a236187988e3f43636e8ec8f
Alice's secret: 15e53462327effedb5da725bfe9610a3d106088d73e522e4
Bob's secret:   15e53462327effedb5da725bfe9610a3d106088d73e522e4
