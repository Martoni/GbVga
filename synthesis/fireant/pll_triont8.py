
N = [float(v+1) for v in range(15)]
M = [float(v+1) for v in range(255)]
O = [float(v) for v in [1,2,4,8]]
Od = [float(v) for v in [2,4,8,16,32,64,128,256]]
Fin = 100/3.

Tolerance = 0.01
Ftarget = 25.0

for vOd in Od:
    for vO in O:
        for vM in M:
            for vN in N:
                Fpfd = Fin/vN
                if (Fpfd >= 10.0) and (Fpfd <= 50.0):
                    Fvco = Fpfd*vM
                    if (Fvco >= 500.0) and (Fvco <= 1500.0):
                        fclkout = (Fvco/vO)/vOd
                        if (fclkout <= (Ftarget + Tolerance)) and (fclkout >= (Ftarget - Tolerance)):
                            print("Ftarget {} found : N{} M{} O{} Od{} [Fpfd {}, Fvco {}]"
                                    .format(fclkout, vN, vM, vO, vOd, Fpfd, Fvco))
