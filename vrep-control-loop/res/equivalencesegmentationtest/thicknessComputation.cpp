      double computeHullThickness(const ForwardIterator &itb, 
                                  const ForwardIterator &ite,
                                  const ThicknessDefinition &def, 
                                  TInputPoint& antipodalEdgeP,
                                  TInputPoint& antipodalEdgeQ,
                                  TInputPoint& antipodalVertexR){
        struct WrapIt{
          WrapIt(const ForwardIterator &b, const ForwardIterator &e): myB(b), myE(e), mySize(std::distance(b, e)){}
          TInputPoint operator[](unsigned int i){
            unsigned int j = i%(mySize);
            return *(myB+j);
          }
          ForwardIterator myB;
          ForwardIterator myE;
          unsigned int mySize;
        };
        WrapIt aConvexHull(itb, ite);
        double resThickness = std::numeric_limits<double>::max();
        unsigned int i = 0;
        unsigned int j = 1;
        unsigned int size = aConvexHull.mySize;
        if(size<3)
          {
          if(size>0)
            {
              antipodalEdgeP = aConvexHull[0];
              antipodalEdgeQ = aConvexHull[size==1?0:1];
              antipodalVertexR = aConvexHull[size==1?0:1];
            }
          return 0.0;
          }
        
        while(getAngle(aConvexHull[i], aConvexHull[i+1], aConvexHull[j], aConvexHull[j+1]) < M_PI ){
          j++;
        }
        double th = getThicknessAntipodalPair(aConvexHull[i], aConvexHull[i+1], aConvexHull[j], def);
        if(th<resThickness){
          resThickness = th;
          antipodalVertexR = aConvexHull[j];
          antipodalEdgeP = aConvexHull[i];
          antipodalEdgeQ = aConvexHull[i+1];
        }
        i++;
        while(i < size){        
          if(getAngle(aConvexHull[i], aConvexHull[i+1], 
                      aConvexHull[j], aConvexHull[j+1])<M_PI){
            j++;
          }else{            
            th = getThicknessAntipodalPair(aConvexHull[i], aConvexHull[i+1], 
                                           aConvexHull[j], def);
            if(th<resThickness){
              resThickness = th;
              antipodalVertexR = aConvexHull[j];
              antipodalEdgeP = aConvexHull[i];
              antipodalEdgeQ = aConvexHull[i+1];
            }
            if(getAngle(aConvexHull[i], aConvexHull[i+1], 
                        aConvexHull[j], aConvexHull[j+1])==M_PI){
              
              th = getThicknessAntipodalPair(aConvexHull[i], aConvexHull[i+1], 
                                             aConvexHull[j+1], def);
              if(th<resThickness){
                resThickness = th;
                antipodalVertexR = aConvexHull[j+1];
                antipodalEdgeP = aConvexHull[i];
                antipodalEdgeQ = aConvexHull[i+1];
              }
            }
            i++;
          }
        }
        return resThickness;
      }
                  

      template<typename TInputPoint>
      inline
      double getAngle(const TInputPoint &a, const TInputPoint &b, const TInputPoint &c, const TInputPoint &d){
        double angle1 = atan2(b[1]-a[1], b[0]-a[0]);
        double angle2 = atan2(d[1]-c[1], d[0]-c[0]);        
        double r =angle2-angle1;
        return  ( r < 0) ? 2*M_PI+r :r;        
      }
      

      template<typename TInputPoint>
      inline
      double getThicknessAntipodalPair(const TInputPoint &p, const TInputPoint &q,
                                       const TInputPoint &r, const ThicknessDefinition &def){
        bool isInside;
        if(def == HorizontalVerticalThickness){
          double dH = computeHProjDistance(p, q, r, isInside);
          double dV = computeVProjDistance(p, q, r, isInside);
          return dH > dV ? dV : dH;
        }else{
          return computeEuclideanDistance(p, q, r, isInside);
        }
      }
	  
	        template< typename TInputPoint>
      inline
      double
      computeHProjDistance(const TInputPoint &a, const TInputPoint &b, const TInputPoint &c, 
                           bool &isInside )
      {
        if(a[1]==b[1])
          {
            return std::numeric_limits<double>::max();
          }
        else 
          {
            auto k = -(a[0]-b[0])*c[1]-(b[0]*a[1]-a[0]*b[1]);
            PointVector<2, double> p (k/static_cast<double>(b[1]-a[1]), 
                                      static_cast<double>(c[1]));
            isInside  = (b-a).dot(p)*(b-a).dot(p)>0;
            return std::abs((k/static_cast<double>(b[1]-a[1])) - c[0]);
          }
      }      
      
      template< typename TInputPoint>
      inline 
      double
      computeVProjDistance(const TInputPoint &a, const TInputPoint &b, 
                           const TInputPoint &c, bool &isInside )
      {
        if(a[0]==b[0])
          {
            return std::numeric_limits<double>::max();
          }
        else 
          {
            auto k = -(b[1]-a[1])*c[0]-(b[0]*a[1]-a[0]*b[1]);
            PointVector<2, double> p (k/static_cast<double>(a[0]-b[0]), 
                                      static_cast<double>(c[0]));
            isInside  = (b-a).dot(p-a)*(b-a).dot(p-a)>0;          
            return std::abs((k/static_cast<double>(a[0]-b[0])) - c[1]);
          }
      }
