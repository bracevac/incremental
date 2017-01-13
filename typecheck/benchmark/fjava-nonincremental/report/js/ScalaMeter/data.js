var ScalaMeter = (function(parent) {
  var my = { name: "data" };
  my.index = [{"scope" : ["DU"], "name" : "Test-0", "file" : "..\/DU.Test-0.dsv"}, {"scope" : ["BU-Early-Cont"], "name" : "Test-1", "file" : "..\/BU-Early-Cont.Test-1.dsv"}, {"scope" : ["DU"], "name" : "Test-2", "file" : "..\/DU.Test-2.dsv"}, {"scope" : ["BU-Early-Cont"], "name" : "Test-3", "file" : "..\/BU-Early-Cont.Test-3.dsv"}, {"scope" : ["DU"], "name" : "Test-4", "file" : "..\/DU.Test-4.dsv"}, {"scope" : ["BU-Early-Cont"], "name" : "Test-5", "file" : "..\/BU-Early-Cont.Test-5.dsv"}];
  my.tsvData = ['date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	19.609432722222216	true	18.144	21.075	ms	"19.2997 19.601643 21.626065 24.448167 19.248376 19.387726 19.743109 20.067359 20.039 20.646807 20.754757 21.437854 19.974074 19.996787 20.088797 20.163668 18.596965 18.664063 18.837225 19.373317 15.64416 18.687899 19.084569 20.255091 18.570434 18.756312 19.159856 20.703584 18.916532 18.952949 19.175931 21.024357 18.388794 18.406572 18.649329 19.56775"\n2017-01-13T10:23:26Z	10	5	2	1	18.510826972222226	true	14.587	22.434	ms	"17.691182 20.482803 21.869769 25.963607 16.415725 16.779678 20.797689 25.061448 16.059318 16.511068 18.974882 24.533355 16.575617 17.045674 25.770835 29.269892 16.154083 16.363026 18.63239 21.895158 16.028411 16.134054 19.327767 20.281531 15.345858 15.387937 15.612157 15.672121 16.088924 16.128692 16.293668 16.826327 15.910558 16.13516 16.163228 16.206179"\n2017-01-13T10:23:26Z	10	5	2	2	13.813843916666666	true	10.434	17.194	ms	"12.224825 12.414604 12.475218 12.760271 12.026426 12.423493 12.450465 12.51065 15.569226 20.663974 21.519539 22.938227 13.01037 13.346112 13.452471 13.677618 12.337954 12.344751 19.211414 21.984621 12.063961 12.304583 12.348612 12.483285 12.259809 12.282667 12.560094 12.577088 12.243955 12.280323 12.47188 12.750262 11.941761 12.01319 12.584058 12.790624"\n2017-01-13T10:23:26Z	10	5	2	3	14.384157222222228	true	10.400	18.368	ms	"12.497885 12.556971 12.779582 12.9223 12.716835 15.585636 17.701391 17.812914 22.845867 22.960797 23.901306 26.081132 13.287593 13.303218 13.598526 13.984435 12.690441 12.75417 12.934832 13.013346 11.989832 12.044977 12.072896 12.309953 12.143137 12.162905 12.225088 12.333853 12.53989 12.78502 13.587695 13.63999 12.753279 12.986145 13.048871 13.276952"\n2017-01-13T10:23:26Z	20	5	2	0	66.91126716666665	true	60.174	73.648	ms	"64.699378 64.992713 66.028439 67.294777 65.852659 66.432053 73.77399 78.13869 62.234812 62.351685 74.508487 93.855324 66.032287 66.396381 71.583673 75.560942 62.880859 64.892775 67.826897 73.569772 62.195145 62.404622 63.493821 64.566458 62.60524 62.741448 64.461188 64.725937 62.754746 63.214301 65.426279 68.088895 62.688995 63.120087 63.352936 64.058927"\n2017-01-13T10:23:26Z	20	5	2	1	65.96712580555555	true	63.476	68.458	ms	"66.22146 66.865815 69.462544 70.691497 65.88934 66.810305 67.026324 68.440634 62.36351 62.482727 64.751822 67.167258 65.653934 67.052392 67.380174 69.339261 65.113672 66.638302 67.31098 67.557679 61.289133 62.944181 63.666919 63.890585 62.537454 62.943596 63.856082 64.000052 65.591936 66.276205 68.219802 68.356335 64.565662 65.552442 68.309199 68.597316"\n2017-01-13T10:23:26Z	20	5	2	2	42.21723830555556	true	40.218	44.217	ms	"42.760962 42.996459 43.75458 44.979274 40.051617 40.581345 43.828129 44.22039 41.231039 41.330655 41.740937 41.856168 46.062351 46.282371 46.282384 46.460446 40.020315 40.078508 40.619279 40.895305 41.234367 41.301868 42.006682 42.449083 41.54825 42.034035 42.301238 42.53152 40.784047 40.827158 40.847358 40.876033 41.104465 41.107631 41.300412 41.533918"\n2017-01-13T10:23:26Z	20	5	2	3	44.16430011111111	true	42.393	45.936	ms	"44.508146 45.132516 45.319048 45.714522 43.122576 43.126039 43.184033 43.19554 42.788903 43.124618 43.16814 43.212188 48.024889 48.028701 48.07688 48.181063 42.794364 43.251941 43.273554 43.353102 43.19752 43.255914 43.257433 43.39927 42.826594 44.137533 45.106634 45.481648 43.442129 43.628584 43.8132 45.56406 42.93675 43.040805 43.090967 43.155"\n2017-01-13T10:23:26Z	40	5	2	0	275.0079297222223	true	265.107	284.908	ms	"275.319573 276.141392 281.276166 287.893469 268.831704 279.99732 280.240597 282.627205 263.332093 267.738995 278.899272 300.213309 270.639313 271.594301 272.005009 274.695222 262.375881 263.250235 264.038027 271.018608 277.576614 279.562802 279.683135 281.990122 263.247233 265.288345 265.685904 269.42807 281.812919 282.102642 284.406177 286.83033 262.064327 262.688524 277.100135 288.6905"\n2017-01-13T10:23:26Z	40	5	2	1	292.10719236111106	true	277.953	306.261	ms	"289.783135 289.880489 297.507493 305.60396 289.503449 289.988149 292.368978 295.391623 288.105979 289.94039 293.217717 298.457663 300.845566 301.563749 303.344923 309.084378 287.282331 289.062362 294.968006 303.283209 272.465735 275.307002 275.432073 281.355437 290.217918 319.079769 321.313202 322.731271 272.195118 273.429248 274.379567 275.893375 284.969726 287.98462 289.479134 290.442181"\n2017-01-13T10:23:26Z	40	5	2	2	157.48636533333334	true	146.944	168.028	ms	"151.613938 151.666872 151.760971 153.64255 151.461752 151.730082 151.935269 151.972036 150.529938 150.537834 156.300921 156.949963 176.72211 176.762622 177.1079 178.527999 152.514389 155.938997 177.378278 182.768168 151.505069 152.725305 153.03087 154.684602 154.700623 155.210502 157.279996 157.287332 156.135703 156.436456 156.86388 158.584965 148.760993 149.012204 149.451878 150.016185"\n2017-01-13T10:23:26Z	40	5	2	3	169.42993719444445	true	160.551	178.309	ms	"162.275462 171.232845 171.239418 171.719566 171.795398 172.006736 172.334632 172.990199 160.481173 169.870863 170.035608 170.605843 179.872889 189.531507 189.534812 189.837826 159.961052 160.416991 162.915181 163.793981 170.540217 171.155649 173.684476 174.700035 159.672889 159.929651 160.025299 160.072023 159.255795 169.827133 170.19805 170.293386 159.375681 168.913901 169.642952 169.73862"\n', 'date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	242.0903871944444	true	233.773	250.408	ms	"236.905591 237.782626 239.029807 239.766495 245.71722 246.122347 247.271438 248.207969 246.020448 247.291608 256.162159 262.547015 243.234369 246.240055 255.334355 255.425495 238.393566 241.711379 247.050321 247.286197 228.868403 229.840044 230.005762 230.639104 236.632247 238.26755 239.679195 241.164912 241.496149 241.599653 242.504999 242.517827 232.071462 234.826302 237.169213 240.470657"\n2017-01-13T10:23:26Z	10	5	2	1	178.75395033333336	true	171.365	186.142	ms	"172.638866 174.242036 175.042161 175.510283 178.848475 179.026015 179.351034 179.603447 179.255822 180.925554 190.575449 194.767981 182.139307 183.304876 189.342888 195.179171 176.448933 178.65058 179.562883 182.195318 168.653485 170.289547 170.707668 171.554528 175.798802 177.20225 178.133712 178.35955 178.703808 179.537293 186.975316 189.1666 170.319673 170.579281 171.193614 171.356006"\n2017-01-13T10:23:26Z	10	5	2	2	73.08241430555555	true	69.618	76.547	ms	"68.262871 71.18443 71.202943 72.867459 72.459607 72.640817 72.814497 73.139978 72.321121 72.594114 72.677122 72.734812 72.712233 75.930817 81.193898 87.718868 71.7032 72.273687 75.218105 75.310074 70.154734 70.226273 72.878713 73.48894 71.754393 71.757155 72.049016 72.115668 72.489254 72.598944 73.377306 73.497639 71.596893 71.804906 71.934783 72.281645"\n2017-01-13T10:23:26Z	10	5	2	3	70.33948486111109	true	68.822	71.857	ms	"67.308547 67.317218 67.324178 67.48427 71.383414 71.394467 71.674533 72.074306 70.845139 70.883045 70.99397 70.998277 71.143026 71.150042 71.199278 71.202202 70.864412 70.947807 71.654614 72.295663 68.140208 68.197992 68.708517 69.47401 70.648833 70.748854 70.875071 71.003258 70.948924 70.981772 71.05994 71.184229 69.792128 69.974711 70.010117 70.334483"\n2017-01-13T10:23:26Z	20	5	2	0	832.6158386944444	true	805.557	859.675	ms	"805.903728 806.338158 806.609538 806.965664 859.124205 869.235573 872.285409 875.87985 851.657189 852.698737 857.740541 858.789078 840.824361 844.889913 847.642483 849.265003 829.783031 833.800807 834.973204 836.958395 793.310114 793.581734 793.61035 794.839526 831.521231 834.34876 835.982183 840.923145 846.341759 850.946537 852.477742 852.815844 797.88655 800.555786 805.699164 807.964901"\n2017-01-13T10:23:26Z	20	5	2	1	543.5911649166668	true	525.843	561.339	ms	"520.678733 520.819533 523.463534 525.604294 553.251654 557.246803 560.655333 563.501733 553.564505 554.005723 557.48594 558.364639 550.019792 551.967449 553.736461 556.752097 541.733039 545.227243 545.876674 548.336799 520.32428 520.782223 525.936582 528.822699 541.644338 542.294532 545.259957 548.034234 551.614205 556.195181 566.277262 585.265268 521.249799 521.332185 525.368403 526.588811"\n2017-01-13T10:23:26Z	20	5	2	2	182.59035547222223	true	176.364	188.817	ms	"171.950101 172.258814 173.798964 174.028962 183.178938 183.357202 183.826908 184.747152 182.003583 182.223047 184.713495 187.493483 180.939163 183.143698 184.254966 184.542298 182.749901 185.095271 185.255131 187.687871 174.570929 175.36042 177.230854 178.043436 180.67412 183.277938 190.648684 198.040527 182.142092 182.541971 186.663362 188.422574 177.568144 178.639582 189.994842 192.184374"\n2017-01-13T10:23:26Z	20	5	2	3	172.7410199166667	true	168.351	177.131	ms	"164.746508 165.205879 165.260136 165.656552 176.034577 176.764475 178.902123 179.638675 174.507304 174.945432 176.153776 176.981872 173.329799 173.382114 173.39481 173.503833 173.635261 173.93476 176.672637 179.822572 167.332919 167.712628 167.804414 168.258498 172.319191 172.492837 172.886239 173.243158 174.518333 174.575118 175.144253 175.699345 169.967618 171.045815 171.268808 171.934448"\n2017-01-13T10:23:26Z	40	5	2	0	3117.731442055555	true	3020.429	3215.034	ms	"3010.484797 3015.057497 3117.117242 3133.158264 3212.744936 3235.010883 3241.307958 3241.772332 3191.043096 3195.969919 3211.631782 3220.801776 3162.912734 3167.667219 3191.192552 3191.212323 3107.628492 3109.341604 3118.415889 3131.587674 2977.938794 2978.659668 2993.550112 2995.5315 3122.226935 3126.754746 3130.383314 3136.361867 3142.541135 3164.240731 3174.059713 3190.634672 2967.850564 2971.443627 2979.708743 2980.386824"\n2017-01-13T10:23:26Z	40	5	2	1	1860.1757711944438	true	1809.438	1910.913	ms	"1779.900134 1787.280127 1805.099316 1817.115363 1898.995791 1907.294129 1912.925236 1929.414348 1903.071357 1905.18915 1905.756729 1906.881222 1871.579898 1879.603801 1880.036621 1884.588078 1855.862853 1856.406551 1856.640346 1863.402822 1778.559596 1780.529892 1781.013622 1783.650852 1856.861662 1861.64093 1867.087946 1875.897351 1876.729006 1890.993242 1899.296974 1908.801727 1780.950299 1831.420461 1884.935237 1900.915094"\n2017-01-13T10:23:26Z	40	5	2	2	513.4385359444445	true	501.489	525.388	ms	"489.399474 490.515355 490.532973 491.9739 523.128428 523.309905 528.589341 529.643012 520.77087 523.376717 524.014044 525.05194 516.553566 516.695302 517.917549 518.111408 513.690395 514.361806 515.766245 517.978468 498.916347 501.041303 501.889535 506.212165 515.455429 517.137221 519.894068 521.170628 519.684718 519.872965 520.343953 520.899543 503.284659 505.074481 510.164167 511.365414"\n2017-01-13T10:23:26Z	40	5	2	3	481.06551369444446	true	469.334	492.797	ms	"458.814593 461.644032 463.17731 465.788374 489.218442 490.565627 490.822932 492.990236 484.590559 489.475901 493.171331 500.087735 483.329999 483.92002 490.686256 494.111933 483.22889 484.803464 486.66778 487.68704 466.479763 466.489083 466.696843 467.078607 481.876641 485.574052 485.825847 486.05896 486.750357 487.111261 487.144768 487.662808 470.265022 472.036167 473.249095 473.276765"\n', 'date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	24.91214155555555	true	19.654	30.170	ms	"23.61429 28.27309 31.234817 33.870459 21.251792 21.366617 25.52549 30.591743 20.495416 20.822979 21.029069 21.574503 20.370681 20.839327 21.017424 22.536503 21.536191 22.739045 24.423169 26.989259 22.070685 26.48833 28.307053 36.204344 26.104458 31.55473 33.202902 36.566509 20.118281 20.166007 20.427857 20.704472 21.559586 23.162455 23.881631 26.215932"\n2017-01-13T10:23:26Z	10	5	2	1	22.180884888888883	true	16.801	27.561	ms	"21.046866 21.888938 21.992192 22.814883 16.96871 20.960186 21.261458 27.668617 16.956459 17.258373 20.653744 22.161245 17.27754 17.999233 18.120359 18.312119 21.333265 21.502688 21.756201 25.115228 16.779456 20.121963 22.711326 25.211526 18.225794 21.785533 22.687277 23.227184 31.386744 32.78327 35.179179 36.739747 17.192984 19.288845 20.960024 21.1827"\n2017-01-13T10:23:26Z	10	5	2	2	16.289417388888886	true	11.582	20.997	ms	"13.345862 13.619931 13.761435 13.866639 15.969784 22.18542 23.12094 23.315483 13.208776 15.099688 23.393397 24.060343 12.587855 12.917255 12.998588 13.269612 14.342228 14.520854 16.092079 18.451997 12.755249 12.960039 13.414031 14.428704 14.457094 14.544222 14.674362 14.725113 15.477832 25.018422 25.338551 25.621664 13.171645 13.177538 13.196063 13.330331"\n2017-01-13T10:23:26Z	10	5	2	3	15.20327988888889	true	12.508	17.898	ms	"14.061678 14.25236 14.257599 14.322634 13.684283 13.844665 14.341211 15.103013 13.522493 13.563506 14.377463 14.409931 13.611847 13.735863 13.796842 13.80566 14.499436 14.540957 14.564867 14.625485 14.119315 16.702081 17.761402 21.723316 14.51135 14.581415 14.625568 14.717112 13.782513 14.160302 16.588792 21.083681 13.634506 13.961659 17.831703 24.611568"\n2017-01-13T10:23:26Z	20	5	2	0	69.3438933611111	true	67.209	71.479	ms	"69.576953 70.138376 71.251685 71.311137 67.462213 67.837982 68.897019 70.464961 66.792431 66.825401 66.861936 67.32572 68.946475 69.405522 69.752178 70.02371 70.666775 70.964345 71.54618 72.025271 66.95994 67.195934 67.719967 67.84379 71.21897 71.709537 72.911781 74.406712 67.203849 69.409848 69.785896 70.68979 66.938945 67.21543 67.921776 69.171726"\n2017-01-13T10:23:26Z	20	5	2	1	70.38306258333333	true	67.858	72.908	ms	"68.039288 68.121524 69.800425 71.558883 69.087603 70.096004 70.166603 70.257406 69.659192 69.968535 70.126403 70.150356 70.644732 71.03107 72.232522 72.677896 70.861493 71.546482 72.220125 73.406145 66.327302 67.540332 68.212792 69.455609 73.242462 74.35008 75.685419 76.035105 70.003759 70.436239 70.456406 71.040822 66.771749 66.942705 67.055397 68.581388"\n2017-01-13T10:23:26Z	20	5	2	2	48.08304380555556	true	42.093	54.073	ms	"46.120333 47.313472 48.054742 48.356052 44.870974 45.377841 46.535677 47.554665 44.505312 44.543781 44.574143 44.609595 44.097234 44.14931 55.89423 58.729365 50.173174 54.103924 64.51687 67.298236 44.456807 44.634757 44.794714 44.842757 48.779107 48.827289 49.174288 50.077527 45.927562 46.253645 46.745665 47.512536 43.421407 43.455783 45.255999 45.450804"\n2017-01-13T10:23:26Z	20	5	2	3	50.051975388888884	true	43.470	56.634	ms	"48.214084 48.79724 50.688315 51.486699 46.319147 46.468666 46.50682 46.514574 47.1809 47.187645 47.212379 47.212793 46.49023 46.675429 46.989016 47.092586 52.482482 52.68158 52.901621 52.95159 47.442706 47.446179 47.482968 47.519885 50.798219 50.809813 50.835858 50.933058 48.225548 58.165253 61.173939 79.865013 46.904537 47.330052 47.392604 47.491686"\n2017-01-13T10:23:26Z	40	5	2	0	308.7816380833333	true	277.815	339.749	ms	"300.630075 302.589991 309.257272 318.21807 278.303952 283.87356 308.149993 310.760449 304.575992 305.302353 307.872499 307.978174 320.215045 325.619853 402.99151 408.578935 324.257661 325.097617 325.171006 326.387462 272.579524 272.796927 273.748567 278.071671 302.803758 303.673832 304.251849 304.398209 290.873419 291.03332 309.833644 318.705817 285.320152 287.930127 303.250247 321.036439"\n2017-01-13T10:23:26Z	40	5	2	1	318.6552429444445	true	280.293	357.018	ms	"310.368838 312.569907 319.701896 319.733329 312.223947 317.886551 318.750941 319.917412 286.585105 287.743139 288.623718 289.014808 291.121268 291.618343 325.144677 327.8201 307.10149 310.211483 311.092929 315.511918 293.494234 404.459679 425.795404 438.803979 304.108816 323.144042 324.039623 337.754161 281.265329 282.535852 328.670846 334.165924 289.09723 311.64595 313.240921 316.624957"\n2017-01-13T10:23:26Z	40	5	2	2	175.3522417222222	true	164.367	186.337	ms	"163.104267 163.682859 163.96274 165.77872 164.946093 165.834693 171.253949 174.443752 171.278886 171.362701 172.557087 172.622739 165.616387 167.503302 172.044625 174.162006 185.289735 191.799606 192.23298 192.734501 169.449405 171.54308 172.097181 172.38686 193.468032 194.717732 194.7362 194.807557 165.599286 166.933459 177.099502 178.239657 171.209719 173.544001 175.468758 179.168645"\n2017-01-13T10:23:26Z	40	5	2	3	193.54871530555553	true	179.285	207.813	ms	"180.691003 180.99933 196.628727 216.725667 177.857819 178.565722 181.683593 183.374651 182.732951 187.624284 194.886486 196.277315 178.284932 188.755796 189.187568 190.04844 205.864501 215.449824 216.134371 216.433665 195.056953 196.353833 197.627521 199.624715 208.280415 208.470842 214.302621 217.276924 177.950207 180.772705 192.197372 192.799787 180.94902 181.246274 181.531666 185.106251"\n', 'date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	266.72371808333327	true	256.533	276.914	ms	"254.404282 259.874497 264.13738 266.631297 268.813287 268.817426 276.089572 278.397992 253.898592 254.05653 257.408135 257.842517 262.572069 263.002998 265.127178 266.02033 266.281433 266.361687 269.888618 270.140499 278.540574 282.213714 289.311525 292.588174 257.804573 257.88326 258.26715 258.585931 253.919006 263.912206 267.029479 268.346993 266.312155 269.478452 273.989324 274.105016"\n2017-01-13T10:23:26Z	10	5	2	1	203.49411244444443	true	196.166	210.822	ms	"197.12582 212.141903 214.549727 218.951367 202.502263 203.146232 205.751153 209.581384 196.605814 196.719123 197.799718 199.229556 200.422699 200.540256 202.625048 202.85166 203.138478 205.332265 208.938938 212.251593 211.340963 212.460494 214.015322 214.970834 198.076012 198.11244 198.125116 198.308558 193.881451 194.20257 194.910366 195.03479 202.236213 202.594173 203.6463 203.667449"\n2017-01-13T10:23:26Z	10	5	2	2	104.20976983333335	true	101.947	106.472	ms	"102.453668 103.51589 103.927 105.61393 103.011061 103.629033 103.818954 103.936079 102.548855 103.169398 103.27788 103.301734 102.711731 102.785375 102.78746 102.825839 107.063051 107.620114 108.297788 110.090519 104.766165 105.465525 105.494116 106.586376 102.641852 104.45479 106.598599 108.782981 101.885286 101.930226 102.422072 102.549261 102.784295 102.84044 102.925449 103.038922"\n2017-01-13T10:23:26Z	10	5	2	3	105.67409136111111	true	103.839	107.510	ms	"103.935624 104.283842 107.135319 110.287401 105.35334 105.398832 105.584452 105.758591 104.110927 104.500663 104.705717 105.090178 104.495687 104.530001 104.931187 104.932804 106.162915 106.493889 106.592324 106.729298 107.047341 107.097573 107.122418 107.241453 107.406784 107.698681 108.429543 108.747836 102.823285 103.247214 103.348651 103.386961 104.129413 104.496756 105.307024 105.723365"\n2017-01-13T10:23:26Z	20	5	2	0	913.2154808888889	true	880.482	945.949	ms	"879.4031 885.124694 890.020022 890.183514 923.475454 926.421077 927.171218 927.547332 886.429604 887.282846 888.594939 894.95831 909.87156 912.236706 916.697537 923.375907 914.146449 914.25508 917.166927 918.696667 976.082014 976.097782 985.703248 990.118415 893.37433 893.593465 897.745009 899.139804 876.658818 877.163644 879.230183 882.104989 924.211814 927.119156 929.681938 934.67376"\n2017-01-13T10:23:26Z	20	5	2	1	627.6436218333333	true	606.939	648.348	ms	"600.744341 604.716975 605.085105 605.842646 630.104193 632.430787 644.028811 649.260124 607.658985 609.171595 615.576356 623.731625 619.062622 636.81336 637.070215 649.829695 630.570075 631.06561 635.115863 640.697463 659.074216 662.430583 664.743875 668.623704 609.561818 612.27933 620.56877 621.89927 598.77373 606.738115 608.933242 613.33989 629.220309 631.171734 638.232175 641.003179"\n2017-01-13T10:23:26Z	20	5	2	2	279.6641749722223	true	262.025	297.303	ms	"272.825817 275.503324 276.655161 279.579511 274.568792 275.25824 276.95046 277.955589 272.172156 275.542417 275.803624 277.003141 275.707349 276.284883 279.165316 283.789644 273.782913 273.844629 274.793094 275.81537 278.197537 309.535357 332.120698 348.916838 273.214561 273.27503 274.562432 274.889329 271.29382 271.341813 271.425391 271.545576 272.121836 272.72456 274.156418 275.587673"\n2017-01-13T10:23:26Z	20	5	2	3	275.12819786111106	true	270.904	279.352	ms	"271.466996 271.568456 271.614531 272.11473 273.559754 275.8179 275.904437 275.910951 270.93876 271.422166 274.118724 275.713156 273.130841 273.49432 273.869173 274.164778 274.525585 276.632566 278.149866 278.767362 276.48579 276.485917 276.718537 277.034032 271.356162 271.470249 281.206094 288.051247 272.018892 276.326178 282.448782 283.732773 271.463207 271.759704 271.8141 273.358407"\n2017-01-13T10:23:26Z	40	5	2	0	3402.2821131111114	true	3268.938	3535.626	ms	"3253.142825 3261.698779 3268.251367 3281.891011 3456.110451 3482.301032 3485.047928 3496.312033 3272.297968 3273.038519 3278.336146 3280.67046 3387.820419 3397.252336 3399.460238 3412.324028 3421.807259 3434.960584 3563.854456 3578.605325 3615.731749 3620.266757 3629.343371 3646.56596 3305.185854 3321.166154 3338.46997 3354.880673 3257.870806 3270.600113 3274.435799 3283.518851 3466.149854 3469.65196 3470.198004 3472.937033"\n2017-01-13T10:23:26Z	40	5	2	1	2136.4186312499996	true	2071.787	2201.050	ms	"2052.573813 2060.1762 2061.39804 2061.769175 2161.058466 2166.29158 2179.971734 2187.140581 2064.808505 2067.111371 2067.375664 2070.287802 2131.623585 2131.833141 2133.912364 2135.554967 2138.201115 2148.22089 2161.404417 2166.722164 2247.797659 2252.807928 2257.082688 2257.174239 2094.608321 2102.721148 2136.269417 2139.237453 2059.195893 2064.350692 2134.449329 2205.208139 2149.701787 2151.342962 2155.449847 2156.237649"\n2017-01-13T10:23:26Z	40	5	2	2	840.1419867222222	true	821.288	858.996	ms	"821.397439 821.815915 824.467476 825.997704 829.234339 843.193109 843.369251 848.593552 816.0758 820.103383 821.052798 823.934436 823.416928 829.9458 865.31285 871.874301 828.591268 830.064701 832.687785 834.564305 862.446172 862.530776 878.520488 883.978928 839.365131 840.935705 846.205844 848.432145 832.790658 846.105823 849.939049 860.241125 826.788275 830.528136 835.201399 845.408728"\n2017-01-13T10:23:26Z	40	5	2	3	816.1553939999998	true	777.318	854.992	ms	"802.970748 803.934122 804.322 804.601221 817.077964 818.224118 904.818873 998.622912 807.115301 812.146878 820.015972 821.957771 796.677459 797.147048 800.103507 802.981617 805.789218 807.528965 818.408769 821.391231 815.824274 816.300696 816.614316 817.627881 805.067815 809.31296 813.74238 815.748101 797.588224 798.033616 803.395893 803.759478 797.536095 798.865638 802.246947 804.094176"\n', 'date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	24.723841194444447	true	19.243	30.205	ms	"21.216827 21.531799 25.076583 30.405286 20.188768 21.318725 21.669277 21.874681 21.612682 22.486931 22.630006 22.818457 21.968857 22.255681 30.996667 43.141762 21.474382 22.511744 30.766733 31.352438 29.403277 30.479418 31.516135 33.347361 21.97903 22.335458 24.224032 27.108517 21.135608 22.281591 22.531733 22.764642 18.2766 21.453315 21.789063 22.134217"\n2017-01-13T10:23:26Z	10	5	2	1	22.493348083333334	true	18.511	26.475	ms	"29.375625 29.795426 30.669211 31.580081 24.193261 24.873833 26.183077 27.099802 18.583446 22.241718 22.624952 23.226589 22.134474 22.35705 23.410716 23.484933 21.755745 21.932379 23.146847 23.262909 19.224626 19.941827 20.624128 21.453991 21.916893 21.997211 22.656957 23.457597 17.599356 18.102988 18.422079 19.377943 18.10416 18.184239 18.332241 18.432221"\n2017-01-13T10:23:26Z	10	5	2	2	14.888406083333333	true	12.770	17.007	ms	"15.238951 16.284939 19.26107 20.995736 14.073904 14.078901 14.743272 15.246362 13.530583 13.669854 13.724363 13.724504 13.963791 14.572905 14.690653 14.810041 13.857731 13.932484 13.944184 14.076391 14.785584 14.823536 14.956735 15.089285 13.482405 13.654492 18.572789 21.062207 13.359152 13.396705 13.42815 13.675079 14.22923 14.318622 14.327862 14.400167"\n2017-01-13T10:23:26Z	10	5	2	3	16.227829611111115	true	11.462	20.994	ms	"22.578381 23.642096 30.832341 33.406928 14.80468 14.852065 14.859938 15.308334 14.662825 14.690043 14.799735 14.819684 14.919336 14.922397 14.991179 15.049755 13.933342 13.951475 14.035945 14.117282 15.722411 16.537064 16.552925 16.654242 13.990059 14.371752 14.5244 14.702189 13.84617 14.306203 14.625154 14.985055 14.369815 14.422459 14.63766 14.776547"\n2017-01-13T10:23:26Z	20	5	2	0	72.76242297222221	true	65.301	80.224	ms	"71.783687 74.476975 74.938889 75.434509 69.660888 70.295504 98.611305 99.392768 69.892218 70.711702 70.864482 71.044826 71.22887 71.888089 73.164526 73.583497 68.611745 69.32665 69.641027 69.860976 71.746082 73.250889 76.071297 78.077557 69.145806 71.558123 72.555514 72.69965 66.903129 66.997269 67.342811 69.007938 69.853137 69.927586 69.933472 69.963834"\n2017-01-13T10:23:26Z	20	5	2	1	72.08383819444443	true	69.726	74.442	ms	"72.920782 73.658418 74.455496 75.411519 68.272303 69.398245 72.433175 73.109927 71.542534 72.801455 73.081471 74.62279 69.678587 70.350821 73.191688 75.704162 67.683974 68.030471 68.21598 69.855144 74.423016 74.581019 74.590406 74.697839 70.509548 70.594192 72.611277 72.963484 70.084512 71.238732 71.388642 72.353335 72.009831 72.715594 72.860691 72.977115"\n2017-01-13T10:23:26Z	20	5	2	2	49.62704313888888	true	45.296	53.958	ms	"48.616733 48.67719 49.995432 51.414469 47.199737 47.210246 47.803963 48.039251 47.541508 47.659684 47.69434 47.72433 47.863012 48.048701 48.246254 48.472627 45.555202 46.918152 47.060237 47.229705 52.207301 52.264316 52.280023 52.360028 45.47221 59.124189 62.427995 62.525441 46.71753 47.807535 48.759993 49.737117 48.274631 48.521999 49.239368 49.883104"\n2017-01-13T10:23:26Z	20	5	2	3	50.66215044444445	true	48.611	52.713	ms	"51.84715 52.544171 54.440971 55.607729 49.297658 49.439654 49.45005 49.464645 49.749003 49.818781 50.217362 50.272441 50.128907 50.443594 50.989152 51.019387 49.260076 49.35259 49.423371 49.457905 52.579071 54.40487 54.444883 54.482308 49.024468 49.494955 51.092537 51.265409 48.652563 48.716541 48.88754 48.990819 49.576711 49.790919 49.802757 50.406468"\n2017-01-13T10:23:26Z	40	5	2	0	304.4126999444445	true	287.838	320.987	ms	"310.035829 310.935129 312.558147 312.625429 290.71431 292.157635 293.439912 294.050461 289.525226 291.184965 292.602103 293.165603 287.103226 287.224363 290.644424 290.837059 283.586438 289.282941 313.215954 314.167044 327.025372 332.135489 333.631355 334.003874 314.310812 314.461978 315.756186 317.036063 287.979445 289.135077 291.421196 292.10645 315.850142 316.574659 318.516981 319.855921"\n2017-01-13T10:23:26Z	40	5	2	1	321.86491622222235	true	309.951	333.778	ms	"326.047687 329.707172 334.213216 335.467009 300.983492 304.370809 324.291036 345.723528 300.993254 302.890293 323.695103 323.903675 325.845179 327.657449 331.505844 336.371285 321.05169 321.808381 323.113698 323.542063 319.244878 320.330482 322.125837 322.78331 312.081589 315.218581 315.767395 321.686166 326.354869 327.137435 327.391492 336.078912 300.95341 301.611351 327.062649 328.126765"\n2017-01-13T10:23:26Z	40	5	2	2	185.48936730555556	true	175.234	195.744	ms	"178.39576 186.213339 190.169134 205.897698 175.861738 184.470295 188.055833 199.118522 177.586766 184.095043 187.184671 187.660501 177.959148 178.050383 179.818067 182.636454 173.404524 173.442051 174.174345 174.971611 202.869 203.220533 204.75923 206.600352 175.53607 181.889374 182.638867 183.749132 183.457272 184.624867 186.07176 187.840923 177.08158 185.926118 186.089208 186.097054"\n2017-01-13T10:23:26Z	40	5	2	3	204.06435794444442	true	191.132	216.997	ms	"195.42345 209.42658 222.237187 235.10988 201.078469 205.532989 205.929949 208.083677 191.891871 203.433138 204.029376 204.385139 188.597965 188.817244 190.198863 192.268004 195.892167 204.341131 206.088325 210.596369 223.236972 224.190352 224.611187 226.182969 205.262231 205.574585 205.717413 206.058856 190.332119 191.266733 193.615972 193.806146 187.673523 201.414806 201.816354 202.194895"\n', 'date	param-roots	param-heights	param-branching	param-naming	value	success	cilo	cihi	units	complete\n2017-01-13T10:23:26Z	10	5	2	0	375.25423883333343	true	341.568	408.940	ms	"367.101016 367.208367 373.547721 379.610186 371.862645 374.465828 375.561351 375.705922 364.585379 365.507441 366.765681 367.876346 363.922005 367.496801 368.163418 374.560869 366.085324 369.780552 439.036211 541.964646 361.352378 362.601793 363.122957 363.695548 366.894337 372.091521 375.236169 376.410281 361.62519 362.742625 365.935853 368.673 364.731507 366.720125 366.86294 369.648665"\n2017-01-13T10:23:26Z	10	5	2	1	307.9400856944444	true	281.933	333.947	ms	"302.042304 302.465442 303.051948 307.224438 306.380143 306.547192 306.907252 309.498885 300.732083 303.581266 389.772592 418.504806 297.82337 298.534224 298.867773 298.938282 299.343776 299.472474 302.607645 306.275618 297.567177 300.830087 306.402866 308.002901 302.681329 302.777214 303.52096 303.613255 296.787584 298.414098 298.956395 300.423354 301.122695 301.191294 302.414644 302.565719"\n2017-01-13T10:23:26Z	10	5	2	2	193.35875661111112	true	189.677	197.041	ms	"191.950129 192.398366 192.493924 192.55098 195.946532 200.289047 200.96002 201.475747 190.067353 190.103244 190.230067 190.299972 189.896265 190.833047 190.847855 191.805134 191.592743 192.367763 192.720882 192.926973 190.051804 190.078455 190.396301 190.555771 196.53578 197.163583 198.482733 200.025264 193.682292 194.365208 194.530594 195.184875 190.463906 191.301429 192.3737 193.9675"\n2017-01-13T10:23:26Z	10	5	2	3	195.7457315555556	true	191.162	200.330	ms	"195.316155 195.499307 199.913968 201.768587 196.700976 197.006878 199.160417 201.89421 191.535374 191.590356 191.62628 192.337018 192.664094 192.696154 192.948836 193.179925 193.571093 194.049115 196.677149 197.021419 192.133068 192.365065 208.397528 209.803501 196.575713 196.686822 196.761386 197.048772 193.241098 193.709489 193.773564 194.485143 193.225469 193.286306 193.852105 194.343996"\n2017-01-13T10:23:26Z	20	5	2	0	1396.650377972222	true	1375.253	1418.048	ms	"1395.943891 1397.037028 1406.49759 1414.948694 1419.723479 1419.926576 1442.75678 1445.571661 1383.917859 1389.01617 1391.897794 1399.640178 1378.256197 1382.869122 1383.382317 1384.000461 1383.549946 1394.92917 1407.232409 1418.968157 1375.52634 1389.176061 1408.347732 1410.362852 1390.88801 1394.61509 1395.395354 1401.514479 1360.773668 1364.685239 1369.012016 1369.489605 1382.944899 1397.782258 1401.241959 1427.592566"\n2017-01-13T10:23:26Z	20	5	2	1	1106.5983479722222	true	1057.586	1155.611	ms	"1088.591235 1091.59181 1096.869446 1097.796854 1113.754348 1120.299089 1203.944709 1288.635505 1086.278163 1091.732595 1099.132573 1108.883107 1078.867803 1082.589261 1084.853345 1087.359606 1083.04564 1091.359599 1154.986011 1235.877364 1081.670289 1088.271332 1092.897768 1094.359573 1102.687148 1103.483229 1105.184373 1106.970992 1071.216825 1074.053032 1076.8198 1080.828768 1087.037357 1087.253878 1095.644279 1102.713821"\n2017-01-13T10:23:26Z	20	5	2	2	714.2382946944443	true	700.215	728.262	ms	"702.054545 708.558862 708.830525 715.072682 721.714643 722.993698 731.606934 734.462491 700.479764 702.003366 706.84802 711.141756 693.981737 698.40608 701.162411 702.229401 708.781369 710.702268 711.1809 711.70442 701.30802 704.035328 705.67324 708.024291 726.644914 734.261282 740.159963 755.323457 714.828753 715.013317 717.596915 719.902914 710.908516 713.87853 720.293802 720.809495"\n2017-01-13T10:23:26Z	20	5	2	3	722.181738	true	688.822	755.541	ms	"704.314586 704.624128 705.757646 707.29954 715.762725 723.442912 724.793776 729.427321 696.255876 698.145997 704.56032 709.183639 695.034545 714.175065 716.923209 717.83728 705.516781 715.049598 735.727243 756.287973 698.731916 706.146377 714.121918 729.59235 723.891916 729.245787 812.304096 858.674019 715.167262 717.994472 720.426873 724.9529 708.074244 715.730736 720.184541 723.183001"\n2017-01-13T10:23:26Z	40	5	2	0	6318.6949525833315	true	6243.757	6393.633	ms	"6299.841984 6308.732567 6321.852733 6342.627371 6452.623245 6468.559765 6481.510928 6508.973126 6264.842788 6293.335606 6301.131249 6306.64364 6221.098248 6231.940238 6266.325742 6282.552559 6230.833449 6262.337543 6286.248419 6293.950311 6253.941045 6270.202475 6294.568614 6296.931235 6332.537882 6350.02952 6371.02763 6381.742236 6243.512164 6274.329011 6292.026932 6299.194031 6343.495386 6344.158884 6348.766878 6350.592859"\n2017-01-13T10:23:26Z	40	5	2	1	5014.124601083333	true	4932.849	5095.400	ms	"4957.150898 4975.055075 4978.283674 4984.890708 5126.26825 5130.693127 5154.827071 5156.240981 4966.112478 4976.194405 5000.481048 5000.931996 4922.017929 4924.474824 4932.716392 4944.578837 4949.738964 5008.328818 5012.307911 5020.843798 4925.33316 4936.08964 4969.096353 4979.985829 5057.513887 5114.060661 5129.906693 5208.45059 4969.12439 4970.687233 5055.918618 5066.388825 4990.817807 4995.96042 5001.491479 5015.52287"\n2017-01-13T10:23:26Z	40	5	2	2	3601.4408819722225	true	3529.402	3673.480	ms	"3530.743629 3537.710006 3538.54423 3548.038029 3660.226143 3667.649423 3718.610442 3729.635543 3535.372689 3541.254569 3548.040403 3551.318553 3517.924679 3544.08919 3575.50447 3583.778941 3529.926256 3536.9043 3551.580131 3555.510322 3540.934112 3548.062758 3560.330471 3574.873913 3688.432402 3699.492161 3710.699689 3713.364078 3629.265838 3636.694621 3653.897658 3662.794737 3614.016583 3624.734909 3634.601778 3657.314095"\n2017-01-13T10:23:26Z	40	5	2	3	3579.900416305556	true	3512.682	3647.118	ms	"3525.56055 3530.087838 3532.134893 3535.971219 3643.998347 3651.550226 3665.556014 3690.977747 3529.626062 3530.27167 3534.076775 3537.331332 3510.142648 3525.61783 3526.832509 3535.871362 3531.041521 3535.641633 3537.039129 3539.412934 3516.618642 3516.779897 3518.983835 3526.147857 3634.329221 3650.06984 3665.828201 3677.850686 3608.878259 3629.904781 3672.095156 3702.218404 3597.742884 3598.812931 3603.639276 3607.772878"\n'];
  parent[my.name] = my;
  return parent;
})(ScalaMeter || {});