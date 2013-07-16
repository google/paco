function Sx(){}
function Zx(){}
function qy(){}
function Gj(a,b){tj(b,a)}
function Rx(a,b){Rvc(b.a,a)}
function Yx(a,b){Svc(b.a,a)}
function py(a,b){Tvc(b.a,a)}
function w5b(a){this.a=a}
function D5b(a){this.a=a}
function Dwc(a){this.a=a}
function jwc(a){this.a=a}
function Zvc(a){a.f=false;Uoc(a.cb)}
function _vc(){awc.call(this,new Bwc)}
function Tvc(a,b){Zvc(a,(a.a,Xw(b),Yw(b)))}
function Rvc(a,b){Xvc(a,(a.a,Xw(b)),Yw(b))}
function Svc(a,b){Yvc(a,(a.a,Xw(b)),Yw(b))}
function Qx(){Qx=Z1c;Px=new jx(n8c,new Sx)}
function Xx(){Xx=Z1c;Wx=new jx(I8c,new Zx)}
function oy(){oy=Z1c;ny=new jx(L8c,new qy)}
function $vc(a){!a.g&&(a.g=Dpc(new jwc(a)));ik(a)}
function Uvc(a){if(a.g){tSc(a.g.a);a.g=null}Yj(a,false)}
function Xvc(a,b,c){if(!Ooc){a.f=true;Voc(a.cb);a.d=b;a.e=c}}
function Bwc(){qwc();ywc.call(this);this.cb[$4c]='Caption'}
function lsc(a,b,c){var d;d=ksc(a,b);!!d&&(d[f9c]=c.a,undefined)}
function Kvc(a,b){var c,d;d=iqc(a.b,b);c=iqc(d,1);return sr(c)}
function Wvc(a,b){BLc(a.cb,z4c,b);Ji(a.a,b+'-caption');BLc(Kvc(a.j,1),b,Ibd)}
function Vvc(a,b){var c;c=b.target;if(pr(c)){return Nr(ur(Kvc(a.j,0)),c)}return false}
function Yvc(a,b,c){var d,e;if(a.f){d=b+Fr(a.cb);e=c+(Hr(a.cb)+$wnd.pageYOffset);if(d<a.b||d>=a.i||e<a.c){return}dk(a,d-a.d,e-a.e)}}
function Xw(a){var b,c;b=a.b;if(b){return c=a.a,(c.clientX||0)-Fr(b)+Jr(b)+(b.ownerDocument,$wnd.pageXOffset)}return a.a.clientX||0}
function Yw(a){var b,c;b=a.b;if(b){return c=a.a,(c.clientY||0)-(Hr(b)+$wnd.pageYOffset)+(b.scrollTop||0)+(b.ownerDocument,$wnd.pageYOffset)}return a.a.clientY||0}
function ck(a){a.w=true;if(!a.s){a.s=$doc.createElement(d5c);a.s.className='gwt-PopupPanelGlass';a.s.style[q6c]=(tu(),r6c);a.s.style[t6c]=0+(qv(),J5c);a.s.style[u6c]=v6c}}
function awc(a){var b,c;fvc.call(this,false,true,V3c);rj(a);this.a=a;c=Kvc(this.j,0);Poc(c,this.a.cb);Gj(this,this.a);ur(sr(this.cb))[$4c]='gwt-DialogBox';this.i=Qr($doc);this.b=0;this.c=0;b=new Dwc(this);kj(this,b,(Qx(),Qx(),Px));kj(this,b,(oy(),oy(),ny));kj(this,b,(Xx(),Xx(),Wx));kj(this,b,(iy(),iy(),hy));kj(this,b,(cy(),cy(),by))}
function s5b(){var a,b,c,d,e,f,g,i,j,k,n;a=(g=new _vc,Wvc(g,'cwDialogBox'),rwc(g.a,'Sample DialogBox'),i=new ZLc,i.e[h9c]=4,Lj(g.j,i),Zj(g),j=new Awc('This is an example of a standard dialog box component.'),WLc(i,j),lsc(i,j,(gAc(),aAc)),k=new Ync((qNb(),fNb)),WLc(i,k),lsc(i,k,aAc),n=new isc(ybd,new D5b(g)),WLc(i,n),eG(),lsc(i,n,fAc),g);ck(a);a.v=true;e=new isc('Show Dialog Box',new w5b(a));d=new Awc('<br><br><br>This list box demonstrates that you can drag the popup over it. This obscure corner case renders incorrectly for many other libraries.');c=new QBc;c.cb.size=1;for(b=10;b>0;--b){MBc(c,Wbd+b,Wbd+b,-1)}f=new ZLc;f.e[h9c]=8;WLc(f,e);WLc(f,d);WLc(f,c);return f}
var Wbd='item ';AIb(291,279,{},Sx);_.xc=function Tx(a){Rx(this,ilb(a,38))};_.Ac=function Ux(){return Px};var Px;AIb(292,279,{},Zx);_.xc=function $x(a){Yx(this,ilb(a,39))};_.Ac=function _x(){return Wx};var Wx;AIb(295,279,{},qy);_.xc=function ry(a){py(this,ilb(a,42))};_.Ac=function sy(){return ny};var ny;AIb(825,1,Q2c,w5b);_.Dc=function x5b(a){Vj(this.a);$vc(this.a)};_.a=null;AIb(826,1,T2c);_.lc=function B5b(){iLb(this.a,s5b())};AIb(827,1,Q2c,D5b);_.Dc=function E5b(a){Uvc(this.a)};_.a=null;AIb(1093,1089,i2c,_vc);_.Lb=function bwc(){try{oj(this.j)}finally{oj(this.a)}};_.Mb=function cwc(){try{qj(this.j)}finally{qj(this.a)}};_._b=function dwc(){Uvc(this)};_.Qb=function ewc(a){switch(Ypc(a.type)){case 4:case 8:case 64:case 16:case 32:if(!this.f&&!Vvc(this,a)){return}}pj(this,a)};_.Db=function fwc(a){Wvc(this,a)};_.ac=function gwc(a){var b;b=a.d;!a.a&&Ypc(a.d.type)==4&&Vvc(this,b)&&(b.preventDefault(),undefined);a.c&&(a.d,false)&&(a.a=true)};_.bc=function hwc(){$vc(this)};_.a=null;_.b=0;_.c=0;_.d=0;_.e=0;_.f=false;_.g=null;_.i=0;AIb(1094,1,_2c,jwc);_.Kc=function kwc(a){this.a.i=a.a};_.a=null;AIb(1095,1096,g2c,Bwc);AIb(1099,1,{38:1,39:1,40:1,41:1,42:1,54:1},Dwc);_.Gc=function Ewc(a){};_.Hc=function Fwc(a){};_.a=null;var Ixb=hTc(_9c,'CwDialogBox$1',825),Kxb=hTc(_9c,'CwDialogBox$3',827),uBb=hTc(X9c,'DialogBox',1093),sBb=hTc(X9c,'DialogBox$CaptionImpl',1095),tBb=hTc(X9c,'DialogBox$MouseHandler',1099),rBb=hTc(X9c,'DialogBox$1',1094),Fpb=hTc(vad,'MouseDownEvent',291),Kpb=hTc(vad,'MouseUpEvent',295),Hpb=hTc(vad,'MouseMoveEvent',292);G3c(wn)(15);