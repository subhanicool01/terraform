namespace_name= "boutique"

echo "Namespace given is "${namespace_name}""
// Lets verify given namespace is exits

if kubectl get namespace "${namespace_name}" &> /dev/null ; then
  echo "your kubecrnerts namespace "${namespace_name}"is exits"
  exit 0
else
  echo "your kubernetes namespace "${namespace_name}" is does not exits so please crete it !!!!!!!"
  if kubectl create namespace "${namespace_name}" $> /dev/null ; then
    echo "your namespace "${namespace_name}" is successfully created !!!!"
    exit 0
  else 
    echo " any error to creating namespace "${namespace_name}""
    exit 1
  fi 
fi  
