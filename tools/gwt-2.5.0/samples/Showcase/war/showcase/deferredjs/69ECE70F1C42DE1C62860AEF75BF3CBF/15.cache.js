function ly(){}
function sy(){}
function Ly(){}
function Rj(a,b){Ej(b,a)}
function ky(a,b){U1b(b.b,a)}
function ry(a,b){V1b(b.b,a)}
function Ky(a,b){W1b(b.b,a)}
function CDb(a){this.b=a}
function JDb(a){this.b=a}
function m2b(a){this.b=a}
function G2b(a){this.b=a}
function a2b(a){a.g=false;YWb(a.db)}
function W1b(a,b){a2b(a,(a.b,qx(b),rx(b)))}
function U1b(a,b){$1b(a,(a.b,qx(b)),rx(b))}
function V1b(a,b){_1b(a,(a.b,qx(b)),rx(b))}
function c2b(){dk();d2b.call(this,new E2b)}
function jy(){jy=aAc;iy=new Ex(ZFc,new ly)}
function qy(){qy=aAc;py=new Ex(uGc,new sy)}
function Jy(){Jy=aAc;Iy=new Ex(xGc,new Ly)}
function b2b(a){!a.i&&(a.i=HXb(new m2b(a)));uk(a)}
function X1b(a){if(a.i){woc(a.i.b);a.i=null}ik(a,false)}
function $1b(a,b,c){if(!SWb){a.g=true;ZWb(a.db);a.e=b;a.f=c}}
function E2b(){t2b();B2b.call(this);this.db[cDc]='Caption'}
function N1b(a,b){var c,d;d=lYb(a.c,b);c=lYb(d,1);return Fr(c)}
function o$b(a,b,c){var d;d=n$b(a,b);!!d&&(d[ZGc]=c.b,undefined)}
function Z1b(a,b){Ahc(a.db,DCc,b);Ui(a.b,b+'-caption');Ahc(N1b(a.k,1),b,wJc)}
function Y1b(a,b){var c;c=b.target;if(Cr(c)){return Zr(Hr(N1b(a.k,0)),c)}return false}
function _1b(a,b,c){var d,e;if(a.g){d=b+rr(a.db);e=c+sr(a.db);if(d<a.c||d>=a.j||e<a.d){return}pk(a,d-a.e,e-a.f)}}
function qx(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientX||0)-Rr(b)+Xr(b)+js(b.ownerDocument)}return a.b.clientX||0}
function rx(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientY||0)-Tr(b)+(b.scrollTop||0)+ks(b.ownerDocument)}return a.b.clientY||0}
function ok(a){a.x=true;if(!a.t){a.t=$doc.createElement(hDc);a.t.className='gwt-PopupPanelGlass';a.t.style[wEc]=(Ou(),xEc);a.t.style[zEc]=0+(Lv(),MDc);a.t.style[AEc]=BEc}}
function d2b(a){var b,c;i1b.call(this,false,true,ZBc);Cj(a);this.b=a;c=N1b(this.k,0);TWb(c,this.b.db);Rj(this,this.b);Tic(Fr(this.db))[cDc]='gwt-DialogBox';this.j=gs($doc);this.c=Vr($doc);this.d=Wr($doc);b=new G2b(this);vj(this,b,(jy(),jy(),iy));vj(this,b,(Jy(),Jy(),Iy));vj(this,b,(qy(),qy(),py));vj(this,b,(Dy(),Dy(),Cy));vj(this,b,(xy(),xy(),wy))}
function yDb(){var a,b,c,d,e,f,g,i,j,k,n;a=(g=new c2b,Z1b(g,'cwDialogBox'),u2b(g.b,'Exemple de bo\xEEte de dialogue'),i=new Yhc,i.f[_Gc]=4,Wj(g.k,i),jk(g),j=new D2b('Ceci est un exemple de composant de bo\xEEte de dialogue standard.'),Vhc(i,j),o$b(i,j,(j6b(),d6b)),k=new aWb((wjb(),ljb)),Vhc(i,k),o$b(i,k,d6b),n=new l$b(nJc,new JDb(g)),Vhc(i,n),sF(),o$b(i,n,i6b),g);ok(a);a.w=true;e=new l$b('Afficher la bo\xEEte de dialogue',new CDb(a));d=new D2b('<br><br><br>Cette zone de liste montre que vous pouvez faire glisser une fen\xEAtre pop-up devant-elle. Ce probl\xE8me complexe se r\xE9p\xE8te pour de nombreuses autres biblioth\xE8ques.');c=new T7b;c.db.size=1;for(b=10;b>0;--b){P7b(c,QJc+b,QJc+b,-1)}f=new Yhc;f.f[_Gc]=8;Vhc(f,e);Vhc(f,d);Vhc(f,c);return f}
var QJc='\xE9l\xE9ment ';Leb(290,278,{},ly);_.Bc=function my(a){ky(this,zU(a,38))};_.Ec=function ny(){return iy};var iy;Leb(291,278,{},sy);_.Bc=function ty(a){ry(this,zU(a,39))};_.Ec=function uy(){return py};var py;Leb(294,278,{},Ly);_.Bc=function My(a){Ky(this,zU(a,42))};_.Ec=function Ny(){return Iy};var Iy;Leb(761,1,UAc,CDb);_.Hc=function DDb(a){fk(this.b);b2b(this.b)};_.b=null;Leb(762,1,XAc);_.qc=function HDb(){ohb(this.b,yDb())};Leb(763,1,UAc,JDb);_.Hc=function KDb(a){X1b(this.b)};_.b=null;Leb(1027,1023,mAc,c2b);_.Qb=function e2b(){try{zj(this.k)}finally{zj(this.b)}};_.Rb=function f2b(){try{Bj(this.k)}finally{Bj(this.b)}};_.ec=function g2b(){X1b(this)};_.Vb=function h2b(a){switch(_Xb(a.type)){case 4:case 8:case 64:case 16:case 32:if(!this.g&&!Y1b(this,a)){return}}Aj(this,a)};_.Ib=function i2b(a){Z1b(this,a)};_.fc=function j2b(a){var b;b=a.e;!a.b&&_Xb(a.e.type)==4&&Y1b(this,b)&&(b.preventDefault(),undefined);a.d&&(a.e,false)&&(a.b=true)};_.gc=function k2b(){b2b(this)};_.b=null;_.c=0;_.d=0;_.e=0;_.f=0;_.g=false;_.i=null;_.j=0;Leb(1028,1,dBc,m2b);_.Oc=function n2b(a){this.b.j=a.b};_.b=null;Leb(1029,1030,kAc,E2b);Leb(1033,1,{38:1,39:1,40:1,41:1,42:1,54:1},G2b);_.Kc=function H2b(a){};_.Lc=function I2b(a){};_.b=null;var U3=kpc(THc,'CwDialogBox$1',761),W3=kpc(THc,'CwDialogBox$3',763),F7=kpc(PHc,'DialogBox',1027),D7=kpc(PHc,'DialogBox$CaptionImpl',1029),E7=kpc(PHc,'DialogBox$MouseHandler',1033),C7=kpc(PHc,'DialogBox$1',1028),$Y=kpc(nIc,'MouseDownEvent',290),dZ=kpc(nIc,'MouseUpEvent',294),aZ=kpc(nIc,'MouseMoveEvent',291);KBc(In)(15);