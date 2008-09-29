public List<ParameterType> getParameterTypes() {
    List<ParameterType> types = super.getParameterTypes();
    types.add(new ParameterTypeInteger("number", 
				       "This is important.", 
				       1, 10, 5));
    return types;
}
