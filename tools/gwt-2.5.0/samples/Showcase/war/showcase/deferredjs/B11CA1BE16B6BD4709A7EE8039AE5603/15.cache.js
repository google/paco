function Sx(){}
function Zx(){}
function qy(){}
function Gj(a,b){tj(b,a)}
function Rx(a,b){F1b(b.a,a)}
function Yx(a,b){G1b(b.a,a)}
function py(a,b){H1b(b.a,a)}
function kDb(a){this.a=a}
function rDb(a){this.a=a}
function r2b(a){this.a=a}
function Z1b(a){this.a=a}
function N1b(a){a.f=false;IWb(a.cb)}
function P1b(){Q1b.call(this,new p2b)}
function H1b(a,b){N1b(a,(a.a,Xw(b),Yw(b)))}
function F1b(a,b){L1b(a,(a.a,Xw(b)),Yw(b))}
function G1b(a,b){M1b(a,(a.a,Xw(b)),Yw(b))}
function O1b(a){!a.g&&(a.g=rXb(new Z1b(a)));ik(a)}
function Xx(){Xx=Nzc;Wx=new jx(aGc,new Zx)}
function Qx(){Qx=Nzc;Px=new jx(HFc,new Sx)}
function oy(){oy=Nzc;ny=new jx(dGc,new qy)}
function I1b(a){if(a.g){hoc(a.g.a);a.g=null}Yj(a,false)}
function L1b(a,b,c){if(!CWb){a.f=true;JWb(a.cb);a.d=b;a.e=c}}
function p2b(){e2b();m2b.call(this);this.cb[OCc]='Caption'}
function _Zb(a,b,c){var d;d=$Zb(a,b);!!d&&(d[zGc]=c.a,undefined)}
function y1b(a,b){var c,d;d=YXb(a.b,b);c=YXb(d,1);return sr(c)}
function K1b(a,b){phc(a.cb,nCc,b);Ji(a.a,b+'-caption');phc(y1b(a.j,1),b,YIc)}
function J1b(a,b){var c;c=b.target;if(pr(c)){return Nr(ur(y1b(a.j,0)),c)}return false}
function M1b(a,b,c){var d,e;if(a.f){d=b+Fr(a.cb);e=c+(Hr(a.cb)+$wnd.pageYOffset);if(d<a.b||d>=a.i||e<a.c){return}dk(a,d-a.d,e-a.e)}}
function Xw(a){var b,c;b=a.b;if(b){return c=a.a,(c.clientX||0)-Fr(b)+Jr(b)+(b.ownerDocument,$wnd.pageXOffset)}return a.a.clientX||0}
function Yw(a){var b,c;b=a.b;if(b){return c=a.a,(c.clientY||0)-(Hr(b)+$wnd.pageYOffset)+(b.scrollTop||0)+(b.ownerDocument,$wnd.pageYOffset)}return a.a.clientY||0}
function ck(a){a.w=true;if(!a.s){a.s=$doc.createElement(TCc);a.s.className='gwt-PopupPanelGlass';a.s.style[eEc]=(tu(),fEc);a.s.style[hEc]=0+(qv(),xDc);a.s.style[iEc]=jEc}}
function Q1b(a){var b,c;V0b.call(this,false,true,JBc);rj(a);this.a=a;c=y1b(this.j,0);DWb(c,this.a.cb);Gj(this,this.a);ur(sr(this.cb))[OCc]='gwt-DialogBox';this.i=Qr($doc);this.b=0;this.c=0;b=new r2b(this);kj(this,b,(Qx(),Qx(),Px));kj(this,b,(oy(),oy(),ny));kj(this,b,(Xx(),Xx(),Wx));kj(this,b,(iy(),iy(),hy));kj(this,b,(cy(),cy(),by))}
function gDb(){var a,b,c,d,e,f,g,i,j,k,n;a=(g=new P1b,K1b(g,'cwDialogBox'),f2b(g.a,'Exemple de bo\xEEte de dialogue'),i=new Nhc,i.e[BGc]=4,Lj(g.j,i),Zj(g),j=new o2b('Ceci est un exemple de composant de bo\xEEte de dialogue standard.'),Khc(i,j),_Zb(i,j,(W5b(),Q5b)),k=new MVb((ejb(),Vib)),Khc(i,k),_Zb(i,k,Q5b),n=new YZb(PIc,new rDb(g)),Khc(i,n),ZE(),_Zb(i,n,V5b),g);ck(a);a.v=true;e=new YZb('Afficher la bo\xEEte de dialogue',new kDb(a));d=new o2b('<br><br><br>Cette zone de liste montre que vous pouvez faire glisser une fen\xEAtre pop-up devant-elle. Ce probl\xE8me complexe se r\xE9p\xE8te pour de nombreuses autres biblioth\xE8ques.');c=new E7b;c.cb.size=1;for(b=10;b>0;--b){A7b(c,qJc+b,qJc+b,-1)}f=new Nhc;f.e[BGc]=8;Khc(f,e);Khc(f,d);Khc(f,c);return f}
var qJc='\xE9l\xE9ment ';oeb(291,279,{},Sx);_.xc=function Tx(a){Rx(this,eU(a,38))};_.Ac=function Ux(){return Px};var Px;oeb(292,279,{},Zx);_.xc=function $x(a){Yx(this,eU(a,39))};_.Ac=function _x(){return Wx};var Wx;oeb(295,279,{},qy);_.xc=function ry(a){py(this,eU(a,42))};_.Ac=function sy(){return ny};var ny;oeb(763,1,EAc,kDb);_.Dc=function lDb(a){Vj(this.a);O1b(this.a)};_.a=null;oeb(764,1,HAc);_.lc=function pDb(){Ygb(this.a,gDb())};oeb(765,1,EAc,rDb);_.Dc=function sDb(a){I1b(this.a)};_.a=null;oeb(1031,1027,Yzc,P1b);_.Lb=function R1b(){try{oj(this.j)}finally{oj(this.a)}};_.Mb=function S1b(){try{qj(this.j)}finally{qj(this.a)}};_._b=function T1b(){I1b(this)};_.Qb=function U1b(a){switch(MXb(a.type)){case 4:case 8:case 64:case 16:case 32:if(!this.f&&!J1b(this,a)){return}}pj(this,a)};_.Db=function V1b(a){K1b(this,a)};_.ac=function W1b(a){var b;b=a.d;!a.a&&MXb(a.d.type)==4&&J1b(this,b)&&(b.preventDefault(),undefined);a.c&&(a.d,false)&&(a.a=true)};_.bc=function X1b(){O1b(this)};_.a=null;_.b=0;_.c=0;_.d=0;_.e=0;_.f=false;_.g=null;_.i=0;oeb(1032,1,PAc,Z1b);_.Kc=function $1b(a){this.a.i=a.a};_.a=null;oeb(1033,1034,Wzc,p2b);oeb(1037,1,{38:1,39:1,40:1,41:1,42:1,54:1},r2b);_.Gc=function s2b(a){};_.Hc=function t2b(a){};_.a=null;var w3=Xoc(tHc,'CwDialogBox$1',763),y3=Xoc(tHc,'CwDialogBox$3',765),i7=Xoc(pHc,'DialogBox',1031),g7=Xoc(pHc,'DialogBox$CaptionImpl',1033),h7=Xoc(pHc,'DialogBox$MouseHandler',1037),f7=Xoc(pHc,'DialogBox$1',1032),BY=Xoc(PHc,'MouseDownEvent',291),GY=Xoc(PHc,'MouseUpEvent',295),DY=Xoc(PHc,'MouseMoveEvent',292);uBc(wn)(15);