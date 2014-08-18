function ND(){ND=anc;MD=new Fkc}
function mUb(a,b,c,d){var e;a.b.dg(b,c);e=oUb(a.b.j,b,c);oj(e,d,true)}
function OD(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ud(c)}}
function QD(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.ld(a);return String(c)}
function SD(){ND();var a;a=YH(MD.pd(azc),60);if(!a){a=new RD;MD.rd(azc,a)}return a}
function PD(c,b){try{typeof $wnd[b]!='object'&&UD(b);c.b=$wnd[b]}catch(a){UD(b)}}
function UD(a){throw new Glc(Lrc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function RD(){this.c='Dictionary userInfo';PD(this,azc);if(!this.b){throw new Glc("Cannot find JavaScript object with the name 'userInfo'")}}
function hkb(){var a,b,c,d,e,f,g,i,j,k,n;f=new Y4b;g=new DRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=frc;g.db.style['textAlign']=yrc;V4b(f,new DRb('<b>This example interacts with the following JavaScript variable:<\/b>'));V4b(f,g);j=new eUb;b=j.k;i=SD();e=(n=new Nkc,OD(i,n),n);a=0;for(d=Jhc(TE(e.b));d.b.Ad();){c=YH(Phc(d),1);k=QD(i,c);XTb(j,0,a,c);mUb(b,0,a,'cw-DictionaryExample-header');XTb(j,1,a,k);mUb(b,1,a,'cw-DictionaryExample-data');++a}V4b(f,new DRb('<br><br>'));V4b(f,j);return f}
var azc='userInfo';L1(342,1,{60:1},RD);_.ld=function TD(a){var b;b="Cannot find '"+a+"' in "+this;throw new Glc(b)};_.tS=function VD(){return this.c};_.b=null;_.c=null;var MD;L1(639,1,Xnc);_.qc=function nkb(){o4(this.b,hkb())};var lN=kcc(avc,usc,342);Koc(In)(32);