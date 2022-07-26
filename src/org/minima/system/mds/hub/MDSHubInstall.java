package org.minima.system.mds.hub;

import java.util.ArrayList;

import org.minima.database.MinimaDB;
import org.minima.database.minidapps.MDSDB;
import org.minima.database.minidapps.MiniDAPP;
import org.minima.system.mds.MDSManager;

public class MDSHubInstall {

	public static final String HUB_START ="<html>\r\n"
			+ "\r\n"
			+ "<head>\r\n"
			+ "    <title>MDS HUB</title>\r\n"
			+ "\r\n"
			+ "<style>\r\n"
			+ "        body {\r\n"
			+ "            font-family: Arial, sans-serif;\r\n"
			+ "            margin: 0;\r\n"
			+ "        }\r\n"
			+ "        .header {\r\n"
			+ "            display: flex;\r\n"
			+ "            justify-content: space-between;\r\n"
			+ "            padding: 10px 20px;\r\n"
			+ "            background: #16181C;\r\n"
			+ "            color: rgba(255, 255, 255, 0.95);\r\n"
			+ "            font-size: 30px;\r\n"
			+ "        }\r\n"
			+ "        .list-container {\r\n"
			+ "            display:flex;\r\n"
			+ "            justify-content: space-around;\r\n"
			+ "            padding-top: 50px;\r\n"
			+ "        }\r\n"
			+ "\r\n"
			+ "        .list-container ul {\r\n"
			+ "            list-style: none;\r\n"
			+ "            padding-left: 0;\r\n"
			+ "        }\r\n"
			+ "\r\n"
			+ "        .list-container li {\r\n"
			+ "            border: 0.5px solid #EDEDED;\r\n"
			+ "            border-radius: 16px;\r\n"
			+ "            margin-bottom: 20px;\r\n"
			+ "        }\r\n"
			+ "\r\n"
			+ "        .list-item-container {\r\n"
			+ "            display: flex;\r\n"
			+ "            justify-content: space-between;\r\n"
			+ "            width: 50vw;\r\n"
			+ "            padding: 20px;\r\n"
			+ "            background: #F4F4F5;\r\n"
			+ "            border-radius: 16px;\r\n"
			+ "            color: #16181C;\r\n"
			+ "            text-decoration: none;\r\n"
			+ "        }\r\n"
			+ "\r\n"
			+ "        .list-item-right {\r\n"
			+ "            display: flex;\r\n"
			+ "            flex-direction: column;\r\n"
			+ "            justify-content: space-between;\r\n"
			+ "            align-items: flex-end;\r\n"
			+ "        }\r\n"
			+ "\r\n"
			+ "        .app-title {\r\n"
			+ "            font-weight: bold;\r\n"
			+ "            font-size: larger;\r\n"
			+ "        }\r\n"
			+ ""
			+ ".solobutton {\r\n"
			+ "		    background-color: #eee;\r\n"
			+ "		    border: 0.5px solid #EDEDED;\r\n"
			+ "		  	border-radius: 8px;\r\n"
			+ "		  	padding: 8px 8px;\r\n"
			+ "		  	font-size: 14px;\r\n"
			+ "		  	height:40px;\r\n"
			+ "		}\r\n"
			+ "\r\n"
			+ "		.solobutton:hover {\r\n"
			+ "			 	cursor: pointer;\r\n"
			+ "		    	color: #fff;\r\n"
			+ "				background-color: #16181C;\r\n"
			+ "		  		border-radius: 8px;\r\n"
			+ "		  		padding: 8px 8px;\r\n"
			+ "				border-color: #16181C;\r\n"
			+ "		}\r\n"
			+ "		\r\n"
			+ "		.solobutton:active {\r\n"
			+ "		  		opacity: 0.8;\r\n"
			+ "				font-style: italic;\r\n"
			+ "		}"
			+ "</style>"
			+ ""
			+ "</head>\r\n"
			+ "\r\n"
			+ "<body>\r\n"
			+ "<center>"
			+ "    <div class=\"header\">\r\n"
			+ "        <img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAABaIAAAUXCAYAAACrmhc4AAAACXBIWXMAAC4jAAAuIwF4pT92AAAgAElEQVR4nOzdTXJj2VYF4CuoaXgOgGfCAGi8LhHVo0WbAfAGQIMBMBHCwBxyICJcWa6yM2VZ0r3nnP3zfRHZyZ6lCMuxtO7ap/P5vAEAAAAAwCh/45UFAAAAAGAkQTQAAAAAAEMJogEAAAAAGEoQDQAAAADAUIJoAAAAAACGEkQDAAAAADCUIBoAAAAAgKEE0QAAAAAADCWIBgAAAABgKEE0AAAAAABDCaIBAAAAABhKEA3k8o9/d/7tHwAAAABpCKKBnATSAAAAAGn84q0CUnsfRv/X/528mQAAAADxnM5nhUIgiVsb0AJpAAAAgFA0ooF6tKQBAAAAQrERDdRmSxoAAABgOY1ooActaQAAAIBlNKKBfrSkAQAAAKZyrBDIYXRwrCUNAAAAMIxGNMCmJQ0AAAAwko1ogPdsSQMAAAAcTiMa4DNa0gAAAACHsBENxBcpDNaSBgAAALibRjTAPbSkAQAAAO5mIxrgEbakAQAAAG6mEQ2wl5Y0AAAAwFUa0QBH0ZIGAAAAuMixQiC27E1jgTQAAACARjTAUFrSAAAAADaiAaaxJQ0AAAA0pRENMJuWNAAAANCMjWggrk7tYYE0AAAAUJhpDoAIzHYAAAAAhZnmAIjEbAcAAABQkEY0QFRa0gAAAEARNqKBmASwl2lJAwAAAAlpRANkoiUNAAAAJGQjGiAjW9IAAABAIhrRANlpSQMAAADBaUQDVKElDQAAAATlWCEQj3bvcQTSAAAAQAAa0QCVaUkDAAAAAdiIBujCljQAAACwiEY0QDda0gAAAMBkNqKBWDR21xBIAwAAAANpRAOgJQ0AAAAMZSMagI9sSQMAAAAH04gG4DItaQAAAOAgGtEAfE1LGgAAANjBsUIgDkFnLlrSAAAAwI00ogF4jJY0AAAAcCNBNAD7CKQBAACALzhWCMAxHDcEAAAAPmEjGohBo7YmgTQAAAC0t2lEAzCUljQAAAC0t9mIBmAaW9IAAADQlkY0AHNpSQMAAEA7GtEArKMlDQAAAC04VgisJ4jkPS1pAAAAKEcjGoBYtKQBAACgHBvRAMRkSxoAAADK0IgGID4taQAAAEjNRjSwlnCRR2lJAwAAQBoa0QDkpCUNAAAAadiIBiA3W9IAAAAQnkY0AHVoSQMAAEBINqKBdQSGzKAlDQAAAMtpRANQm5Y0AAAALGcjGoAebEkDAADAMhrRAPSjJQ0AAABTaUQD0JeWNAAAAEzhWCGwhjYqUQmkAQAA4HCmOQDgPbMdAAAAcDjTHABwidkOAAAAOIxGNAB8RUsaAAAAdrERDcwn0KMCLWkAAAC4mUY0ADxCSxoAAABuZiMaAPawJQ0AAABf0ogGgKNoSQMAAMBFGtEAcDQtaQAAAPjAsUJgLm1RuhJIAwAA0JhGNADMoCUNAABAYzaiAWA2W9IAAAA0oxENAKtoSQMAANCEjWhgHg1Q+JpAGgAAgII0ogEgEi1pAAAACrIRDQBR2ZIGAACgCI1oAIhOSxoAAIDkNKIBIBMtaQAAABJyrBCYQ3AG42hJAwAAEJxGNABkpyUNAABAcDaiAaAKW9IAAAAEpRENABVpSQMAABCIjWhgPGEYrKchDQAAwEKmOQCgA7MdAAAALGSaAwC6MdsBAADAZBrRANCVljQAAACT2IgGxtK6hFwE0gAAAAygEQ0A/ElLGgAAgAFsRAMAl9mSBgAA4CAa0QDAdVrSAAAA7KQRDQDcTksaAACABzhWCIwjrIIetKQBAAD4gkY0ALCPljQAAABfsBENABzDljQAAACf0IgGAI6nJQ0AAMA7NqKBMQRQwI+0pAEAANrSiAYA5tCSBgAAaMtGNAAwly1pAACAdjSiAYB1tKQBAABa0IgGANbTkgYAACjNsULgeNqNwBEE0gAAAGVoRAMAMWlJAwAAlGEjGgCIz5Y0AABAaoJoACAPgTQAAEBKNqKBYwmIgNnMdgAAAISnEQ0A5KYlDQAAEJ5jhQBADY4bAgAAhKURDQDUoyUNAAAQio1o4DhCHyAyLWkAAIBlNKIBgB60pAEAAJaxEQ0A9GJLGgAAYDqNaACgLy1pAACAKTSiAQC0pAEAAIbSiAaOoVEIAAAAwCcE0QAAAAAADCWIBgB45/T07AkPAACAgwmiAQAAAAAYShAN7GcfGiji9N9/660EAAAYQBANAAAAAMBQgmgAAAAAAIYSRAMA/MDBQgAAgGMJogEA7EMDAAAMJYgG9nGoEAAAAIAvCKIBAAAAABhKEA0AAAAAwFCCaAAAAAAAhhJEA4+zDw0UcelQ4enp2e84AACAgwiiAQAAAAAYShANAAAAAMBQgmgAAAAAAIYSRAMArV3ahwYAAOBYgmjgMQ4VAgAAAHAjQTQAwCdOT8++dAMAADiAIBoAAAAAgKEE0QBAW/ahAQAA5hBEA/ezDw0AAADAHQTRAAAAAAAMJYgGAAAAAGAoQTQAwBWnp2dzRAAAADsJooH72IcGinCoEAAAYB5BNAAAAAAAQwmiAQAAAAAYShANAAAAAMBQgmgAoB370AAAAHMJooHbOVQINHV6evb7DwAAYAdBNAAAAAAAQwmiAQAAAAAYShANAAAAAMBQgmjgNvahgSIcKgQAAJhPEA0AAAAAwFCCaACAG5yenj0ZAgAA8CBBNAAAAAAAQwmiAYA27EMDAACsIYgGvuZQIQAAAAA7CKIBAAAAABhKEA0AAAAAwFCCaACghSP2oU9Pz6aKAAAAHiCIBq6zDw0AAADAToJoAAAAAACGEkQDAAAAADCUIBoAAAAAgKEE0QBAeUccKnzjYCEAAMD9BNHA5xwqBAAAAOAAgmgAAAAAAIYSRAMAAAAAMJQgGgAo7ch9aAAAAB4jiAYusw8NAAAAwEEE0QAAdzo9PfuyDgAA4A6CaAAAAAAAhhJEAwAAAAAwlCAa+Jl9aKAIhwqZ5R/+dfPZCQAAV/zixQEAgP3eh9H/82/byUsKAAB/0ogGAIAdLrWhX/9PSxoAAP6kEQ0A8IDT0/P5/O1F65WrtKQBAOA7jWgAoCT70ESjJQ0AQGca0cBHDhUCwFBa0gAAdKQRDQAAD9rbcNaSBgCgC41oAABYTEsaAIDqNKIBACAQLWkAACo6nc/+xgV+Zx8aKGLmocLztxft1cZmBcZa0gAAZKcRDQAAwWlJAwCQnY1oAAB4wIpg2JY0AABZaUQDAEBCWtIAAGSiEQ0AlDJzHxoi0JIGACADjWjgO4cKASA9LWkAAKISRAMA7HB6ehb6NRQ97BVIAwAQjWkOAAAoymwHAABRaEQDAGXYh4bPaUkDALCSRjRgHxoAGtGSBgBgBY1oAAC4Q6VWsZY0AACzaEQDAEBzWtIAAIymEQ0AsNPp6VmjlDK0pAEAGEEjGrqzDw0U4VAhHEtLGgCAI2lEAwAAV2lJAwCwl0Y0AADcqHsYqyUNAMCjNKIBAIC7aUkDAHAPjWgAID370LCOljQAALfQiIbOHCoEOMzp6dnvVNrTkgYA4DMa0QAAcAMB6+20pAEA+JFGNAAAMIyWNAAAm0Y0AAAwg5Y0AEBvGtHQlX1ooAiHCiEfLWkAgH40ogEADvJ6sPD87UXTE26kJQ0A0IdGNAAAfEF7dzwtaQCA2jSiAQCAMLSkAQBq0ogGANKyDw21aUkDANQhiIaOHCoEABIRSAMA5GeaAwAArhCAxmG2AwAgL41oAIADnZ6ehZYwgZY0AEAuGtEAAEBaWtIAADloREM39qGBIhwqBH6kJQ0AEJdGNAAAUIqWNABAPBrRAADwCe3a/LSkAQBi0IgGAADK05IGAFhLIxoASCf6PvTp6Vn7EgLTkgYAmE8jGjpxqBAA4A9a0gAA82hEAwDABRqzvWhJAwCMpRENAADwOy1pAIAxNKIBgFSi70MDdWhJAwAcRyMaurAPDQDwEC1pAID9NKIBAAY4PT37AjAxLVg+oyUNAPAYjWgAAIA7aUkDANxHIxoAAGAHLWkAgK9pREMH9qGBIhwqBCLTkgYA+JxGNAAAwMG0pAEAPtKIBgCAd4SHHElLGgDgO41oAIBBTk/PAk3gD1rSAEBngmgAIAX70EAVAmkAoCPTHFCdQ4UAACGZ7QAAOtGIBgCA32mpsoqWNABQnUY0AABAEFrSAEBVGtEAAAABaUkDAJWczmd/10BZ9qGBIrIfKjx/e9FqTELoR3Ra0gBAVhrRAAAASWhJAwBZ2YgGAABtaJKxJQ0AZKMRDQAAkJiWNACQgUY0ABBa9n1ogFm0pAGAyDSioSqHCgEA2tKSBgCiOZ3P/jaBkgTRQBFVGtHnby/aiYEJ7OhASxoAWEkjGgAAoAEtaQBgJRvRAEBY9qEBjmdLGgBYQSMaKjLLAQDADbSkAYBZNKIBAGhNCAda0gDAeBrRAAATnJ6ehZ1AClrSAMAIGtEAAAD8REsaADiSRjQAEJJDhQBxaEkDAHtpREM1DhUCADCIljQA8CiNaAAA2tLwhMdpSQMA9xBEAwAA8DCBNABwC9McAEA4VfehT0/P5/O3F4+yAyWZ7QAArtGIhkrsQwMAEICWNADwI41oAABaEpLBeFrSAMAbjWgAAACG05IGgN40ogEAAJhGSxoAetKIhirsQwNFVD1UCMDPtKQBoA+NaACAiU5Pz+fztxcNQIB3tKQBoD6NaAAA2tHAhLi0pAGgJo1oAAAAwtGSBoBaNKIBgDDsQwNwiZY0AOSnEQ0VOFQIAEADWtIAkJdGNAAArWhVQg1a0gCQi0Y0AMBkp6fn8/nbiyYfwAG0pAEgB41oAAAAStCSBoC4NKIhO/vQQBEOFQJwFC1pAIhHIxoAAICytKQBIAaNaAAA2hBGQV9a0gCwlkY0AAAArWhJA8B8GtEAwHId96FPT8/n87cXjTyAhbSkAWAejWjIzKFCAAA4hJY0AIwliAYAoAUBE3ALgTQAjGGaAwAAAH5gtgMAjqURDQAs1XEfGoBctKQBYD+NaMjKPjQAAEylJQ0Aj9OIBgBY5PT07EvFSTQZgaNpSQPAfTSiAQAA4EFa0gBwG41oAAAAOICWNAB8TiMaMrIPDRThUCEAFWlJA8DPNKIBAABgEC1pAPhOIxoAgNIEQEAEWtIAdKcRDQCw0OnpWUgK0IyWNAAdaUQDAEvYhwagOy1pADrRiIZsHCoEAIBytKQBqE4jGgCAsoQ6QDZa0gBUpRENAAAAAWlJA1CJIBoAYDEHCwG4RhgNQAWCaMjEPjRQhEOFAAAAvQiiAQAAILD//c9nbw8A6QmiAQAoyaPsQCVmnADIThANAAAAAMBQgmgAYCr70AAAAP0IoiELhwoBSvPINQCX2IcGoApBNAAA5diHBirypSUAmQmiAQAAAAAYShANAAAAAMBQgmjIwD40UIRDhQAAAD0JogEAACCgS4cK7UQDkJUgGgAgCOHCMRwqBACAeATRAAAAAAAMJYgGAKawDw0AANCXIBqic6gQAADaubQP/caUEwAZCaIBACjDPjQAAMQkiAYAAAAAYChBNAAwnH3o23ncGgAAqEgQDZHZhwYAAC7wxSUA2QiiAQAowT40UMW1Q4UAkJUgGgAAAACAoQTRAAAAAAAMJYiGqOxDA0U4VAgAAIAgGgAgGAeoAPq6Zx/a5wUAmQiiAQBIz6FCAACITRANAAAAAMBQgmgAYBj70AAAAGyCaAjKoUIAAOAGdqIByEIQDQBAavahgSruOVQIANkIogEAAtJwAwAAKhFEAwAAAAAwlCAaorEPDRThUCEAzOEpGgAyEEQDAADAYvahAahOEA0AQFoOFQIAQA6CaAAAAAAAhhJEAwCHsw99DJufANzKZwYA0QmiIRKHCgEAoB370AB0IIgGACAl+9AAAJCHIBoAAAAAgKEE0QDAoexDA8AadqIBiEwQDVHYhwbgAqECAABQgSAaAIB07EMDVThUCEAXgmgAAAAAAIYSRAMAAAAAMJQgGiKwDw0U4VAhAKzltgAAUQmiAQAAYAH70AB0IogGAAhOu+0jhwoBACAfQTQAAAAAAEMJogGAQ9iHBoAYPEkDQESCaFjNoUIAAAAAihNEAwCQhn1ooAqHCgHoRhANAAAAAMBQgmgAgATsfQJwD58bAEQjiIaV7EMDRThUCAAAwDWCaAAAAJjIPjQAHQmiAQBIwaFCAADISxANAAAABdmJ7u2vf/2P8+u/7q8DEIcgGgDYxT40AEBcAmkgil+8E7CIQ4UA3Om12Xb+9nLyugEA93ofRv/661/8PQFMpxENAEB49qGBKhwqJAItaWAFjWgAAAAoytM0XKMlDcykEQ0AAABQyCNtZy1pYDSNaFjBPjRQhEOFAAC1aEkDo2hEAwAAwAT2oclGSxo4kkY0AEAiHbc+HSoE2MdONHtpSQNH0IgGAAAAKGJ0g1lLGniUIBoAeIh9aACAvgTSwL1Mc8BsDhUCAEA79qGpymwHcCuNaAAAwrIPDQB5aEkD12hEAwAAQHEOFvYQJQTWkgYu0YgGAO5mH3qt1zCh888PAOShJQ280YiGmexDAwAA0JCWNKARDQBASPahgSocKoSPtKShJ41oAAAAaMBONNFoSUMvGtEAAAAAyWVvGGtJQ30a0TCLfWigCIcKAQAYRUsa6tKIBgBI6PXxau8bQHz2oeFxWtJQi0Y0AADhOFQIMIadaDLSkoYaNKIBAAAAEuvUGtaShrw0ogGAm9mHBgAgAi1pyEcjGmZwqBAAAACG0JKGHDSiAQCSqrrzaR8aqCLqoUI70VSlJQ2xaUQDAAAAUIqWNMSjEQ0AAACQlLD1Oi1piEMjGkazDw0U4VAhAACZaUnDWhrRAAAAcLCo+9Bv7ETTmZY0rKERDQBAGA4VAgAzaUnDPIJoAIDEXhtt3j8A6EmAehyBNIxnmgMA+JJ9aAAAOjDbAeNoRMNIDhUCAEA70fehgdtoScOxNKIBAAjBPjTAXA4Wwm20pOEYGtEAAAAAyWjqrqElDY/TiAYAAACAO2hJw/00omEU+9BAEQ4Vxvf6aHX31wAAYBUtabiNRjQAAAAcJNuhQjvRcBwtabhOIxoAgOUcKgQAKtGShp9pRAMAAAAkIuDMQ0sa/qQRDQB8yj40AAAcQ0ua7jSiYQSHCgEAoJ1s+9Bv7ETDXFrSdKURDQBQwGuIkPWnsA8NAHSlJU0nGtEAAAAASQgta9KSpgONaADgIvvQAAAwn5Y0VWlEw9HsQwMAAMnYiYZ4tKSpRiMaAIBl7EMDVWQ9VAjkoCVNBRrRAAAAAJCAljSZaUQDABTx+li19xIA6tKI5T0tabLRiIYj2YcGinCoEAD6sRMNOWlJk4VGNAAAAOxgHxqIQkuayATRAAAs4VAhAMAYAmkiMs0BAAAAEJxQkUeY7SASjWgA4AP70ADQl8O3UJeWNKtpRMNRHCoEIACHpgAAuEZLmlU0ogEAmM4+NFCFQ4VAZlrSzKQRDQAAAACNaUkzg0Y0AAAAQGAaq8ykJc0oGtFwBPvQQBEOFQIA7g0Am5Y0A2hEAwAAwAPsQwNdaElzBI1oAIBiojfZHCoEAMhJS5o9NKIBAAAAgtJCJSotae6lEQ0A/MY+NADwxk40cCstaW6lEQ17OVQIAADt2IcG+JmWNNdoRAMAMI19aACA+rSkuUQjGgCgoNdHqr2vAJCbZikVaEnzRiMaALAPDQD8xE40cCQtaTSiYQ/70AAAAAB30ZLuSSMaAIAp7EMDVThUCHAMLeleNKIBAAAAgKW0pOsTRAMAAAAXOX67jkCOrgTSdZnmgEfZhwaKcKiwLkemAADIymxHPRrRAAAAcCP70ADzaUnXoBENAMBwDhUCALCXlnRuGtEAAADAp+xEz6f5CV/Tks5HIxoAGrMPDQAAZKYlnYdGNDzCoUIAAACAULSkY9OIBgAo7PVx6vO3l6XNEPvQQBUOFQLkoCUdk0Y0AAAAAFCSlnQcGtEAAADAVRGesOlCYAZjaEmvpxEN97IPDRThUCEAANCRlvQaGtEAAADwBfvQAPVoSc+lEQ0AwDAOFQIAkIGW9Hga0QAAxdn1BOAIPk/GE4LBelrS42hEA0BD9qEBAACu05I+lkY03MOhQgAAAIBWtKSPoRENAMAQ9qGBKhwqBOCNlvTjNKIBAACAm9iJBvhOS/p+GtEAAAAAi2lYQl5a0rfRiIZb2YcGinCosCcNNgAAGEtL+jqNaAAAAPiEfWgAHqEl/TNBNAAAh3OoEKCu16dsvL0AtxFI/8k0BwAAAMBCQiqoz2yHRjQAtGIfGgAAYK2uLWmNaLiFQ4UAANCOfWgARurWktaIBgBoYtamp31ogPrsRAMcq0NLWiMaAAAAYBH70MB7lVvSGtEA0IR9aAAAgDyqtaQ1ouEr9qEBAAAAWKRKS1ojGgCAw9iHBqpwqPBrdqIB5svcktaIBgAAAABIJGNLWiMaAKAR7TUAiMOhQuAIWVrSGtFwjX1ooAiHCgEAAGqL3pLWiAYAAIB37EMDkF3ElrRGNAAAh3CoEKCf18mn87eXFNukAB1FaklrRAMAAABMZh8amG11S1ojGgCKsw/Nj7TXAACgr1UtaY1o+IxDhQAAAAAUNrMlrRENAMBu9qGBKhwqvJ8nbQDym9GS1ogGAAAAAOA3o1rSGtEAAAAAEzlUCGRwdEtaIxousQ8NFOFQIQAAAHsd0ZIWRAMANPS65+l9B/jIPvTjfK4A9LAnkBZEAwCwi0OFAADQyyNhtCAaAAAAYBL70EBXgmgAKMo+NAAAAFEIouFHDhUCAEA79qH3sxMNwDWCaAAAHmYfGgAAuIUgGgCgKc01AJjLPjRQxa+//uV0748iiAYAAAAAYChBNLxnHxoowqFCAGAFT9sA8BlBNAAAAK05VAgA4wmiAQB4iEOFAADQzyP70JsgGgAAAGA8hwqB7gTRAFCMfWjuYcsTAACYQRANbxwqBACAduxDH8+XnABcIogGAOBu9qEBAIB7CKIBAAAABrIPDVTx6KHCTRANALXYhwYAACAiQTRs9qEBAACOZCcagB8JogEAmrs3LLAPDVThUCEAzCOIBgAAAADgqj370JsgGgAAAGAchwoBvhNEg31ooAiHCgGASOxEA/CeIBoAAIB27EMDwFyCaAAAbuZQIQAA8AhBNAAAHp8GgAHsQwNV7D1UuAmiAaAG+9AAQES+6ATgjSCa3hwqBAAAAIDhBNEAANzEPjRQhUOFADCfIBoAAAAAgIuO2IfeBNEAAADASF13oh0qBPhIEE1f9qGBIhwq5CgOSgEAAKMIogEAAGjDPjQArCGIBgDgSw4VAgBAP0ftQ2+CaAAAAIBj2YcG+JkgGgASsw8NAGTgDgEAgmh6cqgQAC4SFACV2YcGgHUE0QAAXGUfGgAA2EsQDQAAAHAQ+9BAFUceKtwE0QCQl31oACAT808AvQmi6cc+NAAAAABMJYgGAOBT9qGBKhwqBIC1BNEAAHzg0WkAAOjt6H3oTRANAAAAzFL9y06HCgE+J4imF/vQQBEOFQIAAJCJIBoAAIDS7EMDwHqCaAAALnKoEAAAOIogGgAAAJim6k60fWigihGHCjdBNADkYx+aGaofkwIAAOYSRNOHQ4UAAAAAsIQgGgCAn/z9P714UYASHCoEgBgE0QAAAMBUJqAAYhq1D70JogEAAAD2cagQ4GuCaHqwDw0U4VAhAAAAGQmiAQAAKMk+NADEIYgGAOADhwoBmMFONEAsI/ehN0E0AAAAwOPsQwPcRhANAEnYhwYAACArQTT1OVQIAAAAAEsJogEA+IN9aKAKhwoBIBZBNAAAALCEg4UAMYw+VLgJogEAAAAe41AhwO0E0dRmHxoowqFCAAAAMhNEAwAAUIp9aACIRxANAMBvHCoEYAU70QBrzdiH3gTRAAAAAPezDw1wH0E0AARnHxoAAIDsBNHU5VAhAAC0Yx8aAGISRAMAYB8agKXsRAPUJ4gGAAAAuIN9aKCKWYcKN0E0AMRmHxoAAIAKBNHUZB8aAAAAAMIQRAMANGcfGqjCocLc7EQD1CaIBgAAAABoZuY+9CaIBgAAALidQ4UAjxFEU499aKAIhwoBAACoQhANAABAevaha7ATDVCXIBoAoDGHCgEAgBkE0QAAAAA3sA8NVDH7UOEmiAaAmOxDAwAAUIkgmlocKgQAAACAcATRAABN2YcGqnCosBYHCwFqEkQDAAAAADSxYh96E0QDAAAAfM2hQoB9BNHUYR8aKMKhQgAAAKoRRAMAAJCWfeia7EQD1COIBgBoyKFCAADoZ9U+9CaIBgAAALjOPjTAfoJoAAjEPjQAAAAVCaKpwaFCAABoxz50bXaiAWoRRAMANGMfGgAAmE0QDQAAAPAJ+9BAFSsPFW6CaAAAAAAARhNEk599aKAIhwoBAD6yEw1QhyAaAKAR+9BAFQ4VAkAugmgAAAAAgMJW70NvgmgAAACAyxwqBDiOIBoAArAPDQBwmZ1ogBoE0eTmUCEAALRjHxoA8hFEAwA04VAhAACwiiAaAAAA4Af2oYEqIhwq3ATRALCefWgAgOvsRAPkJ4gmL/vQAAAAAJCCIBoAoAH70EAVDhUCQE6CaAAAAACAgqLsQ2+CaAAAAICPHCoEOJ4gmpzsQwNFOFQIAHAbBwsBchNEAwAAkIJ9aADISxANAFCcQ4UAAMBqgmgAAACA39mHBqqIdKhwE0QDwDr2oQEA7mMnGiAvQTT5OFQIAAAAAKkIogEACrMPDVThUCEA5CaIBgAAAAAoJNo+9CaIBgAAADIZuRPtUCHAOIJocrEPDRThUCEAAACdCKIBAAAIzT40AOQniAYAKMqhQgAA6CfiPvQmiAYAAACyGbETbR8aYCxBNABMZh8aAACAbgTR5OFQIQAAtGMfGgBqEEQDABRkHxoAAIhEEA0AAACkc+ROtH1ooIqohwo3QTQAzGUfGgAAgI4E0eRgHxoAAAAA0hJEAwAUYx8aqMKhQgCoQxANAAAAAJBc5H3oTRANAAAAZNW/GMIAACAASURBVHXEwUKHCgHmEEQTn31ooAiHCgEAAOhKEA0AAEA49qEBoBZBNABAIQ4VAgAAEQmiAQAAgLT27ETbhwaqiH6ocBNEA8Ac9qEBAADoTBBNbA4VAgAAAEB6gmgAgCLsQwNVOFQIAPUIogEAAIDU9uxEA2SXYR96E0QDAAAAHTlUCDCXIJq47EMDRThUCAAAQHeCaAAAAMKwDw0ANQmiAQAKcKgQgO7sRAMdZdmH3gTRAAAAQDf2oQHmE0QDwED2oQEAAEAQTVQOFQIAAABAGYJoAIDk7EMDVThUyF52ogHiEkQDAAAAbdiHBqrIdKhwE0QDAAAAADCaIJp47EMDRThUCAAAAN8JogEAAFjOPjRHsRMNEJMgGgAgMYcKAQCgn2z70JsgGgAAAOjCoUKAdQTRADCAfWgAAAD4kyCaWBwqBACAduxDA0B9gmgAgKTsQwPAZQ4WAsQjiAYAAADKsw8NVJHxUOEmiAaA49mHBgAAgI8E0cRhHxoAAAAAShJEAwAkZB8aqMKhQkaxEw0QiyAaAAAAACCBrPvQmyAaAAAAqM6hQoD1BNHEYB8aKMKhQgAAAPiZIBoAAIAl7EMzmp1ogDgE0QAAyThUCAAAZCOIBgAAAMqyDw1UkflQ4SaIBoDj2IcGAACAywTRrOdQIQAAAACUJogGAEjEPjRQhUOFANCLIBoAAAAAILDs+9CbIBoAAACo6t//5Z+9twBBCKJZyz40UIRDhQAAAPA5QTQAAABT2YcGgH4E0QAASThUCAAA/VTYh94E0QAAAEBF9qEBYhFEA8BO9qEBAADgOkE06zhUCAAA7diHBoCeBNEAAAnYhwYAADITRAMAAACl2IcGqqhyqHATRAMAAAAAMJogmjXsQwNFOFQIAAAAXxNEAwAEZx8aqMKhQgDoSxANAAAAABBMpX3oTRANAAAAVOJQIUBMgmgAeJB9aAAAALiNIJr5HCoEAIB27EMDQG+CaACAwBwqBAAAKhBEAwAAACXYhwaqqHaocBNEA8Bj7EMDAADA7QTRzGUfGgAAAADaEUQDAARlHxqowqFCAEAQDQAAAAAQRMV96E0QDQAAAFTgUCFAbIJo5rEPDRThUCEAAADcRxANAADAMPahAYBNEA0AEJNDhQAAQCWCaAAAACA1+9BAFVUPFW6CaAC4j31oAAAAuJ8gmjkcKgQAAACAtgTRAADB2IcGqnCoEAB4I4gGAAAAAFis8j70JogGAAAAMnOoECAHQTTj2YcGinCoEAAAAB4jiAYAAOBw9qEBgPcE0QAAgThUCAAA/VTfh94E0QAAAEBW9qEB8hBEA8AN7EMDAADA4wTRjOVQIQAAtGMfGgD4kSAaACAI+9AAAEBVgmgAAAAgHfvQQBUdDhVugmgA+Jp9aAAAANhHEM049qEBAAAAoL1NEA0AEIN9aKAKhwoBgEsE0QAAAAAAC3TZh94E0QAAAEA2DhUC5COIZgz70EARDhUCAADAfoJoAAAADmEfGgD4jCAaAGAxhwoBAIDqBNEAAABAGvahgSo6HSrcBNEA8Dn70AAAAHAMQTTHc6gQAAAAAHhHEA0AsJB9aKAKhwr5//buwDiOXUmiKL/x6856oh/rgwzhRkhPTyJFcrqnATQq6xwnJN5BZwHAV4RoAAAAAICFuu1DvwjRAAAAQBUOFQLUJUQzln1oIIRDhQAAADCOEA0AAMAl9qEBgEeEaACAmzhUCAAA/XTch34RogEAAIAK7EMD1CZEA8A79qEBAABgLCGacRwqBAAAAAA+IEQDANzAPjSQwqFCAOAIIRoAAADYmn1oIEXXQ4UvQjQAAAAAALMJ0YxhHxoI4VAhAAAAjCdEAwAA8BT70ADAUUI0AMBiDhUCAEA/nfehX4RoAAAAYGcOFQJkEKIB4B/2oQEAAGAOIZrrHCoEAIB27EMDAGcI0QAAC9mHBgAAOhKiAQAAgC3ZhwZSdD9U+CJEA8BP9qEBAABgHiGaa+xDAwAAAAAPCNEAAIvYhwZSOFQIAJwlRAMAAAAATGIf+ichGgAAANiOQ4UAWYRonmcfGgjhUCEAAADMJUQDAABwmH1oAOAZQjQAwAIOFQIAAJ0J0QAAAMBW7EMDKRwq/E2IBqA1+9AAAAAwnxDNcxwqBAAAAAAOEqIBACazDw2kcKgQAHiWEA0AAAAAMJh96LeEaAAAAGAbDhUCZBKiOc8+NBDCoUIAAABYQ4gGAADgIfvQAMAVQjQAwEQOFQIAQD/2of8mRAMAAABbsA8NkEuIBqAl+9AAAACwjhDNOQ4VAgBAO/ahAYCrhGgAgEnsQwMAAPwkRAMAAAC3sw8NpHCo8GNCNAAAAAAAUwnRHGcfGgjhUCEAAACsJUQDAExgHxpI4VAhADCCEA0AAAAAMIB96M8J0QAAAMCtHCoEyCdEA9CKfWgAAABYT4jmGIcKAQCgHfvQAMAoQjQAwGAOFQIAALwlRAMAAAC3sQ8NpHCo8GtCNABt2IcGAACAewjRPGYfGgAAAAC4QIgGABjIPjSQwqFCAGAkIRoAAAAA4AL70I8J0QAAAMAtHCoE6EOI5mv2oYEQDhUCAADAfYRoAAAA3rAPDQCMJkQDAAziUCEAAMDHhGgAAABgOfvQQAqHCo8RogGIZx8aAAAA7iVE8zmHCgEAAACAAYRoAIAB7EMDKRwqBABmEKIBAAAAAJ5gH/o4IRoAAABYyqFCgH6EaD5mHxoI4VAhAAAA3E+IBgAA4Af70ADALEI0AMBFDhUCAEA/9qHPEaIBAACAZexDA/QkRAMQyz40AAAA7EGI5m8OFQIAQDv2oQGAmYRoAIAL7EMDAAA8JkQDAAAAS9iHBlI4VHieEA1AJPvQAAAAsA8hmrfsQwMAAAAAgwnRAABPsg8NpHCoEACYTYgGAAAAADjIPvRzhGgAAABgOocKAXoTovnNPjQQwqFCAAAA2IsQDQAA0Jh9aABgBSEaAOAJDhUCAAAcJ0QDAAAAU9mHBlI4VPg8IRqAKPahAQAAYD9CND85VAgAAAAATCJEAwCcZB8aSOFQIQCwihANAAAAAPCAfehrhGgAAABgGocKAXgRovnBPjQQwqFCAAAA2JMQDQAA0JB9aABgJSEaAOAEhwoBAKAf+9DXCdEAAADAFPahAfhFiAYggn1oAAAA2JcQ3Z1DhQAAAADAZEI0AMBB9qGBFA4VAgCrCdEAAADAcPahgRQOFY4hRAMAAAAAMJUQ3Zl9aCCEQ4UAAACwNyEaAACgEfvQAMAdhGgAgAMcKgQAgH7sQ48jRAMAAABDOVQIwHtCNACl2YcGAACA/QnRXTlUCAAA7diHBgDuIkQDADxgHxoAAOAaIRoAAAAYxj40kMKhwrGEaADKsg8NAAAANQjRHdmHBgAAAAAWEqIBAL5gHxpI4VAhAHAnIRoAAAAA4A/2occTogEAAIAhHCoE4DNCdDf2oYEQDhUCAABAHUI0AABAOPvQAMDdhGgAgE84VAgAADCGEA0AAABcZh8aSOFQ4RxCNADl2IcGAACAWoToThwqBAAAAABuIEQDAHzAPjSQwqFCAGAHQjQAAAAAgH3oqYRoAAAA4BKHCgF4RIjuwj40EMKhQgAAAKhHiAYAAAhlHxoA2IUQDQDwjkOFAADQj33ouYRoAAAA4Gn2oQE4QogGoAz70AAAAFCTEN2BQ4UAANCOfWgAYCdCNADAH+xDAwAAjCdEAwAAAE+xDw2kcKhwPiEaAAAAAICphOh09qGBEA4VAgAAQF1CNADAP+xDAykcKgQAdiNEAwAAAABt2YdeQ4gGAAAATnOoEIAzhGgAtmcfGgAAAGoTopM5VAgAAO3YhwYAdiREAwA4VAgAADCVEA0AAACcYh8aSOFQ4TpCNABbsw8NAAAA9QnRqexDAwAAAACbEKIBgPbsQwMpHCoEAHYlRAMAAAAA7diHXkuIBgAAAA5zqBCAZwjRiexDAyEcKgQAAIAMQjQAAEAA+9AAwM6EaACgNYcKAQAA5hOiAQAAgEPsQwMpHCpcT4gGYEv2oQEAACCHEJ3GoUIAAAAAYDNCNADQln1oIIVDhQDA7oRoAAAAAKAN+9D3EKIBAACAhxwqBOAKITqJfWgghEOFAAAAkEWIBgAAKMw+NABQgRANALTkUCEAAPRjH/o+QjQAAADwJfvQAFwlRAOwFfvQAAAAkEeITuFQIQAAtGMfGgCoQogGANqxDw0AALCWEA0AAAB8yj40kMKhwnsJ0QBswz40AAAAZBKiE9iHBgAAAAA2JkQDAK3YhwZSOFQIAFQiRAMAAAAA0exD30+IBgAAAD7kUCEAowjR1dmHBkI4VAgAAAC5hGgAAIBi7EMDANUI0QBAGw4VAgAA3EOIBgAAAP5iHxpI4VDhHoRoAG5nHxoAAACyCdGVOVQIAAAAABQgRAMALdiHBlI4VAgAVCREAwAAAACR7EPvQ4gGAAAA3nCoEIDRhOiq7EMDIRwqBAAAgHxCNAAAQBH2oQGAqoRoACCeQ4UAANCPfei9CNEAAADAv+xDAzCDEA3AbexDAwAAQA9CdEUOFQIAAAAAhQjRAEA0+9BACocKAYDKhGgAAADgB/vQQAqHCvcjRAMAAAAAMJUQXY19aCCEQ4UAAADQhxANAACwOfvQAEB1QjQAEMuhQgAA6Mc+9J6EaAAAAMChQgCmEqIBWM4+NAAAAPQiRFfiUCEAALRjHxoASCBEAwCR7EMDAADsQ4gGAACA5uxDAykcKtyXEA3AUvahAQAAoB8hugr70AAAAABAUUI0ABDHPjSQwqFCACCFEA0AAAAAlGcfem9CNAAAADTmUCEAKwjRFdiHBkI4VAgAAAA9CdEAAAAbsg8NACQRogGAKA4VAgAA7EeIBgAAgKbsQwMpHCrcnxANwBL2oQEAAKAvIXp3DhUCAAAAAMUJ0QBADPvQQAqHCgGANEI0AAAAAFCWfegahGgAAABoyKFCAFYSondmHxoI4VAhAAAA9CZEAwAAbMQ+NACQSIgGACI4VAgAAP3Yh65DiAYAAIBm7EMDsJoQDcBU9qEBAAAAIXpXDhUCAEA79qEBgFRCNABQnn1oAACAvQnRAAAA0Ih9aCCFQ4W1CNEAAAAAAEwlRO/IPjQQwqFCAAAA4EWIBgCqsw8NpHCoEABIJkQDAAAAAKXYh65HiAYAAIAmHCoE4C5CNABT2IcGAAAAfhGid+NQIQAAtGMfGgBIJ0QDAGU5VAgAAFCDEA0AAAAN2IcGUjhUWJMQDcB4//t//lMAAAAA/EuI3ol9aAAAAAAgkBANwBSv3795Fc1U9qGBFA4VAgAdCNEAAAAAQAn2oesSogEAACCcQ4UA3E2I3oV9aCCFQ4UAAADAO0I0AADATexDAwBdCNEAQDkOFQIAANQiRAMwzev3b2Y6AABuZh8aSOFQYW1CNADj2IcGAAAAPiBE78ChQgAAAAAgmBANAJRiHxpI4VAhANCJEA0AAAAAbM0+dH1CNAAAAIRyqBCAXQjRd7MPDaT45FDh6/dvfrUGAACA5oRoAACAxexDAwDdCNEAQBkOFQIAQD/2oTMI0QAAABDIPjQAOxGiAbjuk31oAAAAgBch+mYOFQIAQDv2oQGAjoRoAKZ7/f7Ni2kusw8NAABQlxANAAAAYexDAykcKswhRANwjX1oAAAA4AEh+i72oQEAAACAJoRoAGB79qGBFA4VAgBdCdEAAAAAwHbsQ2cRogFY4vX7N/+BAABYwKFCAHYkRN/BPjSQwqFCAAAA4AAhGgAAYAH70ABAZ0I0ALA1hwoBAADqE6IBAAAghH1oIIVDhXmEaACeYx8aAAAAOEiIXs2hQqCx1+/fxGsAAABoSIgGALZlHxpI4VAhANCdEA0AAAAAbMM+dCYhGgAAAAI4VAjAzoTolexDAykcKgQAAABOEKIBAAAmsg8NACBEA7DY6/dvXlNziEOFAADQj33oXEI0AAAAFGcfGoDdCdEAnGMfGgAAADhJiF7FoUIAAAAAoCkhGgDYjn1oIIVDhQAAPwnRACznYCEAwDj2oYEUDhVmE6IBAAAAAJhKiF7BPjSQwqFCAAAA4AlCNAAAwAT2oQEAfhOiAYCtOFQIAAD92IfOJ0QDAABAUQ4VAlCFEA3AMYP3oV+/f/NrNwAAADQhRM/mUCEAALRjHxoA4C0hGgDYhn1oAACATEI0AAAAFGQfGkjhUGEPQjQAjw3ehwYAAAB6EaJnsg8NAAAAACBEA3Cf1+/fvLTmX/ahgRQOFQIA/E2IBgAAAABuYR+6DyEaAABgIF/8sIJDhQBUI0TPYh8aSOFQIQAAAHCREA0AADDIf//nxQ+4AAAfEKIBgNs5VAgAAJBNiAbgVnY0AUjk3zdmsg8NpHCosBchGoDP2YcGAAAABhCiZ3CoEAAAAADgX0I0AHAr+9BACocKAQA+J0QDAAAAAEvZh+5HiAYAAJjAwUJmcKgQgKqE6NHsQwMpFh4q9Ic6AAAAZBOiAQAALrIPDQDwNSEaALiNQ4UAANCPfeiehGgAAIBJzE8xkn1oACoTogH428J9aAAAACCfED2SQ4UAANCOfWgAgMeEaAC24NPlfuxDAwAA9CFEAwAATOTHVkawDw2kcKiwLyEaAAAAAICphOhR7EMDKRwqBAAAAAYTogGA5exDAykcKgQAOEaIBgAAmMxONADYh+5OiAZgG/5IBwD4m0OFACQQogH4zT40AAAAMIEQPYJDhQAA0I59aACA44RoAGAphwqBrkxQAQCdCdEAAACwKfvQQAqHChGiAfhpk31or8UAAAAgjxB9lX1oAAAAAIAvCdEAwDL2oYEUzx4q9OUPANCVEA0AAAAATGMfmhchGgAAAPbkUCEASYToK+xDAyk2OVQIAAAAZBKiAdiO/UwAdvbsPjQAQGdCNACwhEOFAD/5wRUA6EiIBgAAgM3YhwZSOFTIL0I0QHf2oQEAAIDJhOhnOVQIAAAAAHCIEA0ATGcfGkgx6lChnWgAoBshGoAt+QMdAACgNvvQ/EmIBgAAgI04VAhAIiH6GfahgRQOFQIAAAALCNEAAAAHjNqH/sUMFQDQiRANAEzlUCEAAPRjH5r3hGgAAADYhH1oAFIJ0QBdFdiH9skyAAAAZBCiz3KoEAAA2hm9D/2LH10BgC6EaABgGvvQAAAAvAjRAAAAsAf70EAKhwr5iBAN0FGBfWgAAAAghxB9hn1oAABgMDvRAEAHQjQAW/PHeV32oYEUsw4VAgB0IkQDAAAAAEPYh+YzQjQAAADczKFCANIJ0UfZhwZSOFQIAAAALCZEAwAAfGLVPrSbCABAOiEaABjOoUIAAAD+JEQDsD2vxACAZPahgRQOFfIVIRqgE/vQAAAAwA2E6CMcKgQAACbzBRAAkEyIBgCGsg8NpFh1qBAAoAMhGgAAAAC4xD40jwjRAAAAcBOHCgHoQoh+xD40kKL4oUK7mQB04N87ACCVEA0AAPCOfWgAgLGEaABgGIcKAQCgH/vQHCFEAwAAwA3sQwPQiRAN0EHxfWgA6MRONACQSIj+ikOFAFvxhzkAAADUJEQDAEPYhwZSOFQIADCeEA0AAACL2YcGUjhUyFFCNAAAwGbMUQEAaYToz9iHBlI4VAgAAADcTIgGAAD4h31oAIA5hGgASvGp8p4cKgQAgH7sQ3OGEA0AALAhP77mcqgQgI6EaIBk9qEBAACADQjRH3GoEAAA2rEPDQAwjxANAFxiHxoAAIBHhGgAAABYxD40kMKhQs4SogFSBe9DO94EQBf+zQMAUgjR79mHBgAAAAAYSogGAJ5mHxpI4VAhAMBcQjQAAAAAcJh9aJ4hRAMAAGzMTnQOhwoB6EyI/pN9aCBF8KFCAAAAoB4hGoCSvA4DYBT70AAA8wnRAMBTHCoEAADgKCEaAABgc74Eqs8+NJDCoUKeJUQDpLEPDQAAAGxGiP7FoUIAAAAAgCmEaADgNPvQQAqHCgEA1hCiASjLXiYAnfh3D4C72YfmCiEaAAAAJnKoEACE6J/sQwMpHCoEAAAANiREAwAALdmHBgBYR4gGAE5xqBDgPnaiAbiLfWiuEqIBAABgEvvQAPCTEA2Qouk+tJdhAAAAsD8h2qFCAABoxz40AMBaQjQAcJh9aAAAAJ4hRAMAABRilqoO+9BACocKGUGIBgAAAABgqt4h2j40kKLpoUIAAACgBi+iASjPJ8pr2IcGUjhUCACwnhANAABQjB9hAVjFPjSjCNEAAAAwmEOFAPCWEA1QnX1oAAAAYHN9Q7RDhQAA0I59aACAe3gRDUAEW5lzOVQIsB//9gEAlQjRAAAAMJB9aCCFQ4WMJEQDVGYfGgAAACigZ4i2Dw0AAAAAsIwX0QDAl+xDAykSDxXaiQYAqhCiAQAAAIA37EMzmhANQAyvwgCAuzlUCAAf6xei7UMDKRwqBAAAAIrwIhoAAIiXuA/9iy+CAIAKhGgA4FMOFQIAADCCEA0AAAAD2IcGUjhUyAxCNEBF9qEBAACAQnqFaIcKAeLZyQSgI//+AQC78yIaAPiQfWggRfKhQgCAKoRoAAAAAOAH+9DMIkQDAADARQ4VAsDX+oRo+9BACocKAQAAgGK8iAYAAGJ12od2sBAA2JkQDUAcf4hf51AhAAD0Yx+amYRoAAAAuMA+NAA8JkQDVGIfGgAAACioR4h2qBAAANrptA/9i3kqAGBXXkQDAG/YhwYAAGA0IRoAAACeZB8aSOFQIbMJ0QBV2Ic+xafJAAAAsI/8EG0fGgAAaMSPsQDAjryIBgD+ZR8aSNHxUCEAwM6EaAAAAABozD40KwjRAAAA8ASHCgHguOwQbR8aSOFQIQBwgp1oAGA3XkQDEMsf4QA92YcGANiPEA0A/OBQIQAAALMI0QAAAHCSfWgghUOFrCJEA+zOPjQA8AQTVQDATnJDtEOFAAAAAABb8CIaALAPDcRwqBAAYE9CNADRfJYMAADwMfvQrCREAwAAwAkOFQLAeZkh2j40kMKhQgDgAl8GAQC78CIaAACIYB8aAGBfQjQANOdQIQAA9GMfmtWEaADi+SwZABjFPjQAPEeIBtiVfWgAYAA/yAIAO8gL0Q4VAgAAAABsxYtoAGjMPjSQwqFCAIC9CdEAAABwgH1oIIVDhdxBiAYAAAhnJxoAuFtWiLYPDaRwqHA4f4ADAADAfbyIBgAASrMPDQCwPyEaAJpyqBAAAPqxD81dhGgAAIAGzFRd41AhAFwjRAPsxj40AAAAECYnRDtUCAAA7diHBgCowYtoANrwSfJv9qEBAABYSYgGAABowo+yz7EPDaRwqJA7CdEAO7EPDQAAAATKCNH2oQEAAAAAtuVFNAA0Yx8aSOFQIQBAHUI0AABAI3aiAXqyD83dhGgAWvHHNwBwhkOFADBG/RBtHxpI4VAhAAAAEMqLaAAAoBz70AAAtQjRANCIQ4UAAADcQYgGAABoxs2EY+xDAykcKmQHQjTADuxDAwAAAMFqh2iHCgF4gldgAAAAsJYX0QDQhH1oIIVDhQAA9QjRAAAADflCCKAH+9DsQogGAACAdxwqBICx6oZo+9BACocKAQAAgHBeRAMAAGXYhwYAqEmIBqClbruYDhUC8BE70QDZ7EOzEyEaAAAA/mAfGgDGE6IB7mQfGgAAAGigZoh2qBAAANqxDw0AUJcX0QAQzj40AF+xEw0ArCBEAwAAwD/sQwMpHCpkN0I0AG15AQYAAABr1AvR9qGBFA4VAgAAAE14EQ0AwexDAykcKpzLV0IAwGxCNAAAAAAEsQ/NjoRoAAAAcKgQAKYSogHuYB96Gz5FBgAAgPlqhWiHCgEAoB370AAA9XkRDQChHCoE4AxfCQEAMwnRAAAAtGcfGkjhUCG7EqIBVrMPDQAAADRTJ0TbhwYAAAAAKMmLaADaS9zEtA8NpHCocC070QDALEI0AAAAAASwD83OhGgAAABac6gQAOarEaLtQwMpHCoEAAAAGvIiGgAA2JJ96HvYiQYAZhCiASCMQ4UAAADsRogGAK+/AKAt+9BACocK2Z0QDbCKfWgAAACgqf1DtEOFAAAAS/lSCAAYzYtoAAhiHxpI4VAhAEAWIRoAAAAACrMPTQVCNAAAAC05VAgA6+wdou1DAykcKizBHiYA/ObfRQBgJC+iAQCArdiHBgDII0QDQAiHCgEAoB/70FQhRAMAANCOfWgAWEuIBpjNPjQAUJSdaABglH1DtEOFAADQjn1oAIBMXkQDwB+qvvyyDw0AAMDOhGgAAABasQ8NpHCokEqEaICZ7EMDAAAAbBqi7UMDAABswcFCAGAEL6IBoDj70EAKhwoBAHIJ0QAAAABQjH1oqhGiAeAdnyADQC6HCgHgHvuFaPvQQAqHCgGAEH6kBQCu8iIaAAC4nX1oAIBsQjQAFOZQIQAAABUI0QAAALRgHxpI4VAhFQnRADPYhwYAwtiJBgCu2CtEO1QIwCb8sQ0AAADjeBENAEXZhwZSOFQIAJBPiAYAAACAIuxDU5UQDQAAwCGVp6scKgSAe+0Tou1DAykcKgQAAAB4w4toAPiEg4UA89mHBgDoQYgGgIIcKgQAgH7sQ1OZEA0AAMBhFb8Ysg8NAPcTogFGsg8NAAAA8Jc9QrRDhQAAAAAAsbyIBoBi7EMDKRwqBADoQ4gGgC9U3MEEAH6zDw2kcKiQ6oRoAAAATvFDLQBw1v0h2j40kMKhQgAAAIAPeRENAAAsZx8aAKAXIRoACnGoEAAA+rEPTQIhGgAAgNMq7EQ7VAgA+xCiAUawDx3NQSYAAAC45t4Q7VAhAAC0Yx8aAKAfL6IBoAj70AAAAFQlRAMAAPCUneer7EMDKRwqJIUQDXCVfWgAAACAL90Xou1DAwAAAAC04EU0ABxw96fH9qGBFA4VAgD0JEQDAADwtJ13ogGqsw9NEiEaAACAKA4VAsB+7gnR9qGBFA4VAgAAADzkRTQAALCEfWgAgL6EaADYnEOFAOzOTjQAGK+sfgAABHhJREFU8IgQDQAH+SMbAPZnHxpI4VAhaYRogGfZhwYAAAA4ZH2IdqgQAAAAAKAVL6IBYGP2oYEUDhXmM2EFAHxFiAYAAACAjdiHJpEQDQAAQASHCgFgX2tDtH1oIIVDhW357BgAAADO8yIaAACYyj40AABCNABsyqFCAKrx5RDAdfahSSVEAwAAUJ59aADYmxANcJZ9aAAAAIBT1oVohwoBAKAd+9AAALx4EQ0A563Yv7QPDUBVdqIBgI8I0QAAAJRmHxpI4VAhyYRoAAAAAACmWhOi7UMDKRwqBAAAADjNi2gA2Ix9aCCFQ4V92YkGAN4TogHgCf7ABgAARrIPTTohGgAAgLIcKgSAGoRogKPsQwMAAAA8ZX6IdqgQAADasQ+NGSsA4E9eRAPARhwqBAAAIJEQDQAAQEn2oYEUDhXSgRANcIR9aD7gk2MAAAA4Zm6Itg8NAADQlh9tAYBfvIgGgE3YhwZSOFQIAMB7QjQAAAAA3MQ+NF0I0QAAAJTjUCEA1DIvRNuHBlI4VAgAAABwiRfRAHCBI0wAb9mH5j3/VgIAL0I0AOzBoUIAAACSCdEAAACUYh8aSOFQIZ0I0QBfsQ8NAAAAcNmcEO1QIQAAAP+wEw0AeBENADezDw2kcKgQAIDPCNEAcJFXXgAAwFn2oelGiAYAAKAMhwoBoKbxIdo+NJDCoUIAgGF8QQQAvXkRDQAAXGYfGgCArwjRAHAjhwoBAKAf+9B0JEQDAABQgn1oAKhLiAb4iH1oTrJ7CQCP+fcSAPoaG6IdKgQAgHbsQwMA8IgX0QBwE/vQAAAAdCFEAwAAsD370EAKhwrpSogGeM8+NADANHaiAaCncSHaPjQAAAAAAB/wIhoABjnzwss+NJDCoUIAAI4QogEAAABgAfvQdCZEAwAAsNTZnWiHCgGgvjEh2j40kMKhQgAAAIDhvIgGAACeYh8aAICjhGgAWMyhQgAAALoRogFgoLOblwDA1+xDAykcKqQ7IRrgF/vQAADL+PEWAHq5HqIdKgQAAAAA4AteRAPAQvahgRQOFQIAcIYQDQAAAAAT2YcGIRoAhrN5CQDHPPo306FCAMhxLUTbhwZSOFQIAAAAMI0X0QAAwCn2oQEAOEuIBoBFHCoEAIB+7EPDT0I0AAAAt/lsJ9o+NABkEaIB7EMDAAAATPV8iHaoEAA+9dnrLgAAAOjIi2gAWMA+NJDCoUIAAJ4hRAMAAHCr918S2YcGUjhUCL8J0QAAAAAATPVciLYPDaRwqBAAAABgOi+iAQCAQ+xDAwDwLCEaACb5tXfpUCEAPPZ+JxqgOvvQ8JYQDQAAwDYcKgSATEI00Jd9aAAAAIAlzodohwoBAKAd+9AAAFzhRTQATGQfGgAAAF5e/vP66oEzAAAAAADzeBENAAAAAMBUQjQAAAAAAFMJ0QAAAAAATCVEAwAAAAAwlRANAAAAAMBUQjQAAAAAAFMJ0QAAAAAATCVEAwAAAAAwlRANAAAAAMBUQjQAAAAAAFMJ0QAAAAAAzPPy8vL/GhbE3AWgKmIAAAAASUVORK5CYII\" width=\"30\" height=\"30\"/>\r\n"
			+ "        \r\n"
			+ "        <div>MDS</div>\r\n"
			+ "    </div>\r\n"
			+ "<br><br><br>"
			+ "<div class='app-title'>Application Installed..</div>"
			+ "    <div class=\"list-container\">\r\n"
			+ "        <ul>";
	
	public static String createHubPage(MDSManager zMDS, MiniDAPP zDAPP, String zPassword) {
		
		//Start the HTML
		String page = HUB_START;
		
		//Get the DB
		MDSDB db = MinimaDB.getDB().getMDSDB();
		
		String base = "./"+zDAPP.getUID()+"/";
		
		page +=   "<li>\r\n"
				+ "                <div class=\"list-item-container\">\r\n"
				+ "                    <img width='50' src='"+base+zDAPP.getIcon()+"'>\r\n"
				+ "\r\n"
				+ "                    <div class=list-item-right>\r\n"
				+ "                        <div class=\"app-title\">"+zDAPP.getName()+"</div>\r\n"
				+ "                        <div>"+zDAPP.getDescription()+"</div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "            </li>"
				+ "</div>";
	
		//Now the return form..
		page += 
				"	<form action=\"login.html\" method=\"post\">\r\n"
				+ "		<input type='hidden' name='password' value='"+zPassword+"'/>\r\n"
				+ "		<input class='solobutton' style='width:200;' type=\"submit\" value='Back to MDS Hub' onClick=\"this.form.submit(); this.disabled=true; this.value='Checking..';\"/>\r\n"
				+ "	</form>\r\n"
				+ "</center>"
				+ "</html>";
		
		return page;
		
	}
}
